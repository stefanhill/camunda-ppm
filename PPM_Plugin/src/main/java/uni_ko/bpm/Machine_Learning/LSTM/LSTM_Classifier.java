package uni_ko.bpm.Machine_Learning.LSTM;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.deeplearning4j.eval.ROC;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration.GraphBuilder;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.MultiNormalizerMinMaxScaler;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.shade.guava.base.Stopwatch;
import org.nd4j.shade.guava.collect.Lists;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;
import uni_ko.bpm.Data_Management.DataSetCalculations.FieldCreation.TaskUID;
import uni_ko.bpm.Data_Management.DataSetCalculations.RiskCalculations.RiskQuadratic;
import uni_ko.bpm.Data_Management.Data_Reader.Token_Reader;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Classifier_Exception;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.MetricImpl.External_Metric_DL4J_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;

public class LSTM_Classifier extends Classifier{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1696238718514994723L;

	public transient ComputationGraph network;
	
	public List<String> fields;
	protected int steps;

	

	private long elapsed_time = 0;
	private int unique_task_count = 0;
	private Data_Set ref_ds = null;
	private MultiDataSetPreProcessor preProcessor = null;
	@Override
	public Metric train(Data_Set training_set, Data_Set test_set) throws Exception {
		ComputationGraph buffered_best_network = this.network;
		if(training_set.get_flow_count() < this.batch_size) {
			throw new Classifier_Exception("Batch-Size should not be bigger than the Trainingset!");
		}
		
		training_set = new Token_Reader(training_set, this.steps, 0, 
				new ArrayList<DataSetCalculation>(Arrays.asList(new RiskQuadratic(), new TaskUID(training_set)))
			).get_instance_flows();
		training_set.postProcess();
		test_set = new Token_Reader(test_set, this.steps, 0, 
				new ArrayList<DataSetCalculation>(Arrays.asList(new RiskQuadratic(), new TaskUID(test_set)))
			).get_instance_flows();
		test_set.postProcess();
		this.ref_ds = (training_set.get_unique_Task_Name().size() > test_set.get_unique_Task_Name().size()) ? training_set : test_set;
		this.training_allowed = true;
		this.unique_task_count = Math.max(training_set.get_unique_Task_Name().values().size(), test_set.get_unique_Task_Name().values().size());
		SamplingMultiDataSetIterator_imp mdsi_training = new SamplingMultiDataSetIterator_imp(training_set, this.build_fields_imp(), this.batch_size, unique_task_count);
		this.preProcessor = mdsi_training.getPreProcessor();
		SamplingMultiDataSetIterator_imp mdsi_testing  = new SamplingMultiDataSetIterator_imp(test_set, this.build_fields_imp(), this.batch_size, unique_task_count);

		int one_hot_width = Math.max(training_set.get_unique_Task_Name().size(), test_set.get_unique_Task_Name().size());
		this.network = new ComputationGraph(this.get_Conf(one_hot_width, (MultiDataSet)mdsi_testing.to_MultiDataSet()));
		this.network.init();


		int fittet_frequency = (int) (Math.floor(mdsi_training.size()/this.batch_size) * this.update_frequency);
		fittet_frequency = 1;

		Pair<Double, Integer> best_epoch = new Pair(Double.MAX_VALUE, 0);

		while(		this.training_allowed
					&& this.max_epoch > 0  
					&& (best_epoch.getSecond() + this.max_epoch_without_improvement) > this.network.getEpochCount()
					&& ( 	this.last_calculated_metric == null || 		// avoid error for non-existing evaluation
							this.last_calculated_metric.accuracy() < 0.99
					)
			) {
			Stopwatch stopwatch = Stopwatch.createStarted();
			
			this.network.fit(mdsi_training);
			
			stopwatch.stop(); 
			Long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
			
			if(this.network.getEpochCount() % fittet_frequency == 0 ) {
				try {
					this.last_calculated_metric = new External_Metric_DL4J_Wrapper(this.network.doEvaluation(mdsi_testing, new Evaluation(), new RegressionEvaluation()), this.network.numParams());
				}catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}
			}
			if(this.network.score() < best_epoch.getFirst()) {
				buffered_best_network = this.network.clone();
				best_epoch = new Pair(this.network.score(), this.network.getEpochCount());
			}		
			
			elapsed_time += time;
			this.max_epoch--;
		}	
		this.ref_ds.data_set = null;
		this.network = buffered_best_network;
		this.last_calculated_metric = new External_Metric_DL4J_Wrapper(this.network.doEvaluation(mdsi_testing, new Evaluation(), new RegressionEvaluation()), this.network.numParams());
		return this.last_calculated_metric;

	}

	private int batch_size;
	private int update_frequency;
	
	private int hidden_layer_count_spl;
	private int hidden_layer_count_mer;
	private double adam_lern;
	private double adam_beta1;
	private double adam_beta2;
	private double adam_epsilon;
	
	private int max_width;
	private int max_epoch;
	private int max_epoch_without_improvement;
	
	@Override
	public List<Parameter_Communication_Wrapper> configurational_parameters() {
		List<Parameter_Communication_Wrapper> parameters = new ArrayList<Parameter_Communication_Wrapper>();
		
		parameters.add(new Parameter_Communication_Wrapper_Lists<PredictionType>(
						0, 
						"Prediction-Types", 
						"Defines which types this classifier will predict.", 
						false,
						this.prediction_types,
						PredictionType.class, 
						Arrays.asList(PredictionType.ActivityPrediction, PredictionType.TimePrediction, PredictionType.RiskPrediction), 
						true
						)
				);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
						1, 
						"Batch-Size", 
						"This defines how large a single batch will be.", 
						false,
						this.batch_size,
						Integer.class, 
						100)
				);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				2, 
				"Feedback-Frequency", 
				"The higher the frequency the more time is needed to calculate the feedback.", 
				true,
				this.update_frequency,
				Integer.class, 
				10)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				3, 
				"Number of splitted Hidden-Layers", 
				"The number of layers which will learn type specific knowledge.",
				false,
				this.hidden_layer_count_spl,
				Integer.class, 
				3)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				4, 
				"Number of merged Hidden-Layers", 
				"The number of layers which will learn commone knowledge.", 
				false,
				this.hidden_layer_count_mer,
				Integer.class, 
				1)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				5, 
				"Maximum width", 
				"The maximum width of a layer. Helps against hughe computation times.", 
				false,
				this.max_width,
				Integer.class, 
				256)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				6, 
				"Maximum Epochs", 
				"Maximum number of epochs befor the training is terminated.", 
				true,
				this.max_epoch,
				Integer.class, 
				1000)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				7, 
				"Maximum epochs without improvement", 
				"Helps angainst overfitting.", 
				false,
				this.max_epoch_without_improvement,
				Integer.class, 
				25)
		);
		
		parameters.add(new Parameter_Communication_Wrapper_Single<Double>(
				8, 
				"Adam learning rate", 
				"Optimizer Hyperparameter", 
				true,
				this.adam_lern,
				Double.class, 
				1e-3)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Double>(
				9, 
				"Adam beta1", 
				"Optimizer Hyperparameter", 
				true,
				this.adam_beta1,
				Double.class, 
				0.9)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Double>(
				10, 
				"Adam beta2", 
				"Optimizer Hyperparameter", 
				true,
				this.adam_beta2,
				Double.class, 
				0.999)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Double>(
				11, 
				"Adam epsilon", 
				"Optimizer Hyperparameter", 
				true,
				this.adam_epsilon,
				Double.class, 
				1e-8)
		);
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                12,
                "Steps",
                "Steps back in the event log",
                false,
                this.steps,
                Integer.class,
                8)
        );
		
		return parameters;
	}

	@Override
	public void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception {
		// ActivityPrediction is mandatory and mandatory on position 0
        try {
            this.prediction_types = ((List<String>) parameters.get(0)).stream()
                    .map(PredictionType::valueOf)
                    .collect(toList());
        } catch (Exception e) {
            this.prediction_types = ((List<PredictionType>) parameters.get(0));
        }
		
		if(!this.prediction_types.contains(PredictionType.ActivityPrediction)) {
			this.prediction_types.add(0, PredictionType.ActivityPrediction);
		}else if(this.prediction_types.get(0) != PredictionType.ActivityPrediction){
			this.prediction_types.remove(PredictionType.ActivityPrediction);
			this.prediction_types.add(0, PredictionType.ActivityPrediction);
		}
		
		this.batch_size 					= (int) parameters.get(1);
		this.update_frequency				= (int) parameters.get(2);
		
		this.hidden_layer_count_spl			= (int) parameters.get(3);
		this.hidden_layer_count_mer			= (int) parameters.get(4);
		
		this.max_width						= (int) parameters.get(5);
		this.max_epoch						= (int) parameters.get(6);
		this.max_epoch_without_improvement 	= (int) parameters.get(7);

		this.adam_lern						= (double) parameters.get(8);
		this.adam_beta1						= (double) parameters.get(9);
		this.adam_beta2						= (double) parameters.get(10);
		this.adam_epsilon					= (double) parameters.get(11);
		
		this.steps							= (int) parameters.get(12);
	}

	private List<Field_Config> build_fields_imp() {
		// L< T<string, one_hot_in, one_hot_out> >
		List<Field_Config> extended_fields = new ArrayList<Field_Config>();
		for(PredictionType pt : this.prediction_types) {
			switch (pt) {
			case ActivityPrediction:
				// Pair.create(0, 1) -- featur the current task_uid - label the next task_uid
				extended_fields.add(new Field_Config("task_uid", true, true, true, true, Activation.HARDSIGMOID, Activation.SOFTMAX, new Pair(0, 1), LossFunctions.LossFunction.MSE));
				break;
			case TimePrediction:
				// Pair.create(-1, 0) -- feature the time needed by the previous task - lable the time needed by this task
				extended_fields.add(new Field_Config("duration", true, false, true, false, Activation.HARDSIGMOID, Activation.SIGMOID, new Pair(-1, 0), LossFunctions.LossFunction.MSE));
				break;
			case RiskPrediction:
				// Pair.create(null, 0) -- don't care about input - label the current risk
				extended_fields.add(new Field_Config("RiskQuadratic", false, false, true, false, Activation.HARDSIGMOID, Activation.SIGMOID, new Pair(null, 0), LossFunctions.LossFunction.MSE));
				break;
			default:
				break;
			}
		}
		return extended_fields;
	}

	

	private ComputationGraphConfiguration get_Conf(	int one_hot_width, MultiDataSet mds) {

		List<Field_Config> ext_fields =  this.build_fields_imp();

		GraphBuilder c = new NeuralNetConfiguration.Builder()
						.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
						.updater(new Adam(this.adam_lern, this.adam_beta1, this.adam_beta2, this.adam_epsilon))
				        .graphBuilder();
        
        // generate names
        List<String> names = new ArrayList<String>();
        for(Field_Config fc : ext_fields) {
        	names.add(fc.field_name);
        }
		String[] inputs 	= new String[mds.getFeatures().length];
		String[] outputs 	= new String[mds.getLabels().length];
		
		for(int i = 0; i < mds.getFeatures().length; i++) {
			inputs[i] 	= names.get(i)+"_input";
		}
		for(int i = 0; i < mds.getLabels().length; i++) {
			outputs[i] 	= names.get(i)+"_output";
		}
		
		c.addInputs(inputs);
		
		List<String> name_buffer = new ArrayList<String>();
		int[] input_nout = new int[ext_fields.size()];
		int input_width = 0;
        for(int i = 0;i < ext_fields.size(); i++) {
        	if(ext_fields.get(i).has_input) {
	        	name_buffer.add("I1-"+i);
	        	// If 
	        	input_nout[i] = Math.min(this.max_width, Math.max(128, (int) Math.pow(((ext_fields.get(i).one_hot_in) ? one_hot_width : 1), 2)));
	        	input_width += input_nout[i];
	        	
	        	c.addLayer(		name_buffer.get(i), 
	        					new LSTM.Builder()
	        					.nIn(((ext_fields.get(i).one_hot_in) ? one_hot_width : 1))
	        					.nOut(input_nout[i])
	        					.activation(ext_fields.get(i).hidden_activation)
	        					.build(), inputs[i]);
        	}
        }

        c.addVertex("merge", new MergeVertex(), name_buffer.stream().toArray(String[]::new));
        String last_name = "merge";
        for(int depth = 0; depth < this.hidden_layer_count_mer; depth++) {
        	String buf = "Merge"+depth;
        	c.addLayer(buf, new LSTM.Builder().nIn(input_width).nOut(input_width).activation(Activation.HARDSIGMOID).build(), last_name);
        	last_name = buf;
        }
        String last_name_buf = last_name;
        for(int i = 0;i < ext_fields.size(); i++) {
        	last_name = last_name_buf;
        	int io_dif = input_width - (ext_fields.get(i).one_hot_out ? one_hot_width : 1);
        	int steping = io_dif / (this.hidden_layer_count_spl+2);
        	int previous_out = input_width;
        	for(int depth = 0; depth < this.hidden_layer_count_spl; depth++) {
        		String buf = "Layer"+i+"-"+depth;
        		int buf_nout = previous_out - steping;
        		c.addLayer(buf, new LSTM.Builder()
        							.nIn(previous_out)
        							.nOut(buf_nout)
        							.activation(ext_fields.get(i).hidden_activation)
        							.build(), last_name);
        		last_name = buf;
        		previous_out = buf_nout;
        	}
	        c.addLayer(outputs[i], new RnnOutputLayer.Builder()
	                				.lossFunction(ext_fields.get(i).loss_function)
	                				.activation(ext_fields.get(i).output_activattion)
	                				.nIn(previous_out)
	                				.nOut((ext_fields.get(i).one_hot_out ? one_hot_width : 1))
	                				.build(), last_name);
        }

        
        c.setOutputs(outputs);
        c.backpropType(BackpropType.TruncatedBPTT);
        ComputationGraphConfiguration conf = c.build();

		return conf;
	}
	
	
	
	
	
	@Override
	public List<Classification> evaluate(Data_Set data_set) throws Exception {
		data_set = new Token_Reader(data_set, this.steps, 0, 
				new ArrayList<DataSetCalculation>(Arrays.asList(new RiskQuadratic(), new TaskUID(data_set)))
			).get_instance_flows();
		this.ref_ds.data_set = data_set.data_set;
		
		MultiDataSetIterator mdsi = new SamplingMultiDataSetIterator_imp(this.ref_ds, this.build_fields_imp(), 1, this.unique_task_count, this.preProcessor);
		
		List<Classification> classifications = new ArrayList<Classification>();
		List<String> node_names = Lists.newArrayList((data_set.get_unique_Task_Name().keySet()));
		
		
		while (mdsi.hasNext()) {
			MultiDataSet batch = (MultiDataSet) mdsi.next();
			
			INDArray[] batch_output = new INDArray[0];
			if(batch.getFeatures(0).length() >= 1) {
				INDArray[] test = batch.getFeatures();
				batch_output  = this.network.output(batch.getFeatures());
				((MultiNormalizerMinMaxScaler)mdsi.getPreProcessor()).revertLabels(batch_output);
			}
		    
		    for(int i = 0; i < batch_output.length; i++) {
			    long[] shape =  batch_output[i].shape();
			    float[][] output_list = batch_output[i].get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(shape[shape.length-1]-1)).toFloatMatrix();
			    for(int batch_index = 0; batch_index < output_list.length; batch_index++) {
			    	float[] single_output = output_list[batch_index];
			    	classifications.add(new Classification(single_output, this.ref_ds.get_unique_Tokens(), this.prediction_types.get(i)));
			    }
		    }
		}
		this.ref_ds.data_set = null;
		return classifications;
	}

	@Override
	public List<PredictionType> get_prediction_type() {
		return this.prediction_types;
	}
	
	@Override
    public <T> T importSER(String path) throws Exception{
        LSTM_Classifier classifier = super.importSER(path);
        classifier.network = ComputationGraph.load(new File(this.path_manipulator(path)), true);
        return (T) classifier;
    }

	@Override
    public void exportSER(String path) throws Exception {
        File check_file = new File(this.path_manipulator(path));
        check_file.getParentFile().mkdirs();
        this.network.save(check_file);
        this.add_subfile(check_file);
        
        super.exportSER(path);
    }
	
	private String path_manipulator(String path) {
        int index = path.lastIndexOf(File.separator);
        return new StringBuilder(path).replace(index, index+1,File.separator+"DL4J-Graphs" + File.separator).toString();
	}

}
