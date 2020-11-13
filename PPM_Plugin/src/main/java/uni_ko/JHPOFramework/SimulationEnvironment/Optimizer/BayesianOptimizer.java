package uni_ko.JHPOFramework.SimulationEnvironment.Optimizer;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import javax.management.InvalidAttributeValueException;

import org.apache.commons.lang3.ArrayUtils;

import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.DB_Utils.Table_Updates;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Config_Utils;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.CheckMethods;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Annotations.OptimizerOrdering;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;
import uni_ko.bpm.Reflections.ReflectionReads;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.evaluation.RegressionAnalysis;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.pmml.jaxbbindings.NeuralNetwork;

public class BayesianOptimizer extends OptimizationAlgorithm implements Serializable, Cloneable{
	public enum SupportedClassifier{
		LinearRegression("LinearRegression", new LinearRegression()),
		RandomForest("RandomForest", new RandomForest()),
		IBK("IBK", new IBk());
		
		public String name;
		public weka.classifiers.Classifier classifier;
		SupportedClassifier(String name, weka.classifiers.Classifier classifier){
			this.name = name;
			this.classifier = classifier;
		}
	}
	
	private weka.classifiers.Classifier classifier;
	
	private Instances trainingData;
	
	private ArrayList<Attribute> attributes = new ArrayList<>();
	public HashMap<Integer, List<String>> attributeIndexer = new HashMap<Integer, List<String>>();
	protected transient Pair<String, Method> method;
	
	private List<HashMap<Integer, Object>>  previousConfigurations = new ArrayList<HashMap<Integer,Object>>();
	
	
	private boolean trained = false;
	
	private int epochsWithoutImprovement;
	private double minImprovement;
	private double prevBestScore = -1.0;
	private double bestScore = 0.0;
	private int sinceBestScore = 0;
	private int optimizationRuns;

	public BayesianOptimizer(){}
	public BayesianOptimizer(
			int epochsWithoutImprovement,
			double minImprovement,
			Method method,
			int maximumGenerations,
			int optimizationRuns,
			SupportedClassifier supportedClassifier
			) throws InvalidAttributeValueException {
		this.epochsWithoutImprovement = epochsWithoutImprovement;
		this.minImprovement = minImprovement;
		CheckMethods.checkMethod(method);
		this.method = new Pair<String, Method>(method.getName(), method);
		this.maximumGenerations = maximumGenerations;
		this.optimizationRuns = optimizationRuns;
		this.classifier = supportedClassifier.classifier;
	}
	
	@Override
	public void initAlgorithm(
			Integer optimizerID,
			Pair<List<Integer>, 
			List<Range>> configuration, 
			Integer dataID,
			Class<? extends Classifier> classType,
			SaveDefinition saveDefinition,
			MetricConfiguration metricConfiguration, 
			Connection db_con, 
			List<Integer> tbmIDs
			) throws Exception {
		
		super.initAlgorithm(optimizerID, configuration, dataID, classType, saveDefinition, metricConfiguration, db_con, tbmIDs);

		this.epochsWithoutImprovement = epochsWithoutImprovement;
		this.minImprovement = minImprovement;
		CheckMethods.checkMethod(method.getValue());
		this.method = method;
		
		int i = 0;
		for(Range r : configuration.getValue()) {
			List<String> attVals = (List<String>) r.options.stream()
										.map(o -> o.toString())
										.collect(Collectors.toCollection(ArrayList::new));
			this.attributeIndexer.put(i, attVals);
			this.attributes.add(new Attribute("Parameter"+i, attVals));
		//	attributes.add(new Attribute("Parameter"+i, false));
			i++;
		}
		this.attributes.add(new Attribute("Result", false));
		this.trainingData = new Instances("TrainingData", this.attributes, 0);
		this.trainingData.setClassIndex(this.trainingData.numAttributes() - 1);
		
		saveDefinition.setClassifierDistribution(this.db_con, this.maximumGenerations, this.classType);
		saveDefinition.validate(metricConfiguration.relevant_metric_methods);		
	}
	
	@Override
	public boolean hasNext() throws Exception {
		if(this.bestScore >= 1.0) {
			return false;
		}else {
			return 	this.sinceBestScore < this.epochsWithoutImprovement && 
					(this.bestScore - this.prevBestScore) >= this.minImprovement &&
					this.maximumGenerations > this.previousConfigurations.size();
		}
	}

	@Override
	public Pair<Integer, HashMap<Integer, Object>> nextConfigurationString(Metric metric, HashMap<Integer, Object> configuration) throws Exception {
		double metricValue = 0.0;
		if(metric != null) {
			metricValue = (double) this.method.getValue().invoke(metric, null);
		}
		if(this.bestScore < metricValue) {
			this.prevBestScore = this.bestScore;
			this.bestScore = metricValue;
			this.sinceBestScore = 0;
		}else {
			this.sinceBestScore ++;
		}
		
		if(configuration != null) {
			double[] values = new double[this.configuration.getValue().size()+1];
			int pos = 0;
			for(Entry<Integer, Object> entry : configuration.entrySet()) {
				int index = this.attributeIndexer.get(entry.getKey()).indexOf(entry.getValue().toString());
				values[pos] = index;
				pos++;
			}
			values[pos] = metricValue;
			
			Instance instance = new DenseInstance(1.0, values);
			this.trainingData.add(instance);
			instance.setDataset(this.trainingData);
			
	        if (this.trained && this.classifier instanceof weka.classifiers.UpdateableClassifier) {
	               ((UpdateableClassifier) this.classifier).updateClassifier(instance);
	        } else if(this.trainingData.size() >= 2){
	        	this.classifier.buildClassifier(this.trainingData);
	        	this.trained = true;
	        }
		}
        HashMap<Integer, Object> nextConfiguration = this.findBestConfiguration();
        
        System.out.println("Next Config: "+nextConfiguration);
        return new Pair<Integer, HashMap<Integer, Object>>(this.saveToDB(Config_Utils.to_json(nextConfiguration)), nextConfiguration);
	}
	
	private HashMap<Integer, Object> findBestConfiguration() throws Exception{
		Pair<Double, HashMap<Integer, Object>> best = new Pair<Double, HashMap<Integer,Object>>(0.0, null);
		Random rnd = new Random(); 
		for(int i = this.optimizationRuns; i >= 0; i--) {
			HashMap<Integer, Object> potentialConfiguration = null;
			
			double[] values = new double[this.attributeIndexer.size()+1];
			int index = 0;
			int infinityLoopCounter = 0;
			
			while(potentialConfiguration== null) {
				potentialConfiguration = new HashMap<Integer, Object>();
				index = 0;
				for(Entry<Integer, List<String>> entry : this.attributeIndexer.entrySet()) {
					int random = rnd.nextInt(entry.getValue().size());
					
					values[index] = random;
					potentialConfiguration.put(index, this.configuration.getValue().get(index).options.get(random));
					index++;
				}
				if(infinityLoopCounter >= 2* this.optimizationRuns) {
					return null;
				}else if(this.previousConfigurations.contains(potentialConfiguration)) {
					potentialConfiguration = null;
				}
			}
			values[index] = 0.0;
			if(this.trained) {
				Instances instances = new Instances("PredictionData", this.attributes, 0);
				instances.setClassIndex(instances.numAttributes() - 1);
				Instance inst = new DenseInstance(1.0, values);
				inst.setDataset(instances);
				
				double prediction = this.classifier.classifyInstance(inst);

				if(this.newIsBetter(prediction, best.getKey())) {
					best = new Pair<Double, HashMap<Integer,Object>>(prediction, potentialConfiguration);
				}
			}else {
				// if classifier is not yet trained - use this random configuration
				this.previousConfigurations.add(potentialConfiguration);
				return potentialConfiguration;
			}

		}
		this.previousConfigurations.add(best.getValue());
		return best.getValue();
	}
	
	
	private Integer saveToDB(String configuration) throws Exception {
		return Table_Updates.registerNewConfiguration(this.db_con, configuration, this.classType, this.dataID, this.optimizerID).intValue();
	}
	@Override
	public Integer getMaxCases() {
		return this.maximumGenerations;
	}
	
	
	@Override
	public List<Parameter_Communication_Wrapper> configurational_parameters() throws Exception {
		List<Parameter_Communication_Wrapper> parameters = new ArrayList<Parameter_Communication_Wrapper>();

		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				0, 
				"Maximum epochs without improvement", 
				"Helps angainst overfitting.", 
				false,
				this.epochsWithoutImprovement,
				Integer.class, 
				10)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Double>(
				1, 
				"Minimum improvement", 
				"Helps angainst overfitting.", 
				false,
				this.minImprovement,
				Double.class, 
				0.01)
		);
		parameters.add(new Parameter_Communication_Wrapper_Lists<String>(
				2, 
				"Optimization-Method", 
				"Defines on which method the optimization will be done.", 
				false,
				null,
				String.class, 
				ReflectionReads.getMetricMethodsAsString(), 
				false
				)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				3, 
				"Simulationsize", 
				"The maximum number of simulations done.", 
				false,
				this.maximumGenerations,
				Integer.class, 
				20)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
				4, 
				"Optimization runs", 
				"Betters the convergence but worsens the runtime.", 
				false,
				this.optimizationRuns,
				Integer.class, 
				20)
		);
		parameters.add(new Parameter_Communication_Wrapper_Lists<SupportedClassifier>(
				5, 
				"Optimization-Classifier", 
				"Defines on which classifier which will performe the optimization.", 
				false,
				null,
				SupportedClassifier.class, 
				Arrays.asList(SupportedClassifier.values()), 
				false
				)
		);
		
		return parameters;
	}

	@Override
	public void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception {
		this.epochsWithoutImprovement = (int) parameters.get(0);
		this.minImprovement = (double) parameters.get(1);
		this.method = new Pair<String, Method>(((List<String>) parameters.get(2)).get(0), ReflectionReads.getMetricMethod(((List<String>) parameters.get(2)).get(0)));
		this.maximumGenerations = (int) parameters.get(3);
		this.optimizationRuns = (int) parameters.get(4);

        try {
            this.classifier = ((List<String>) parameters.get(5)).stream()
                    						.map(SupportedClassifier::valueOf)
                    						.collect(toList()).get(0).classifier;
        } catch (Exception e) {
        	this.classifier = ((List<SupportedClassifier>) parameters.get(5)).get(0).classifier;
        }

	}
	
	
    private void writeObject(ObjectOutputStream out) throws IOException, SecurityException
    {
    	this.methodStr = new Pair<String, String>(method.getKey(), method.getValue().getName());
    	out.defaultWriteObject();
       
    }
    protected Pair<String, String> methodStr;
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException
    {
    	in.defaultReadObject();
    	this.method = new Pair<String, Method>(methodStr.getKey(), Metric.class.getMethod(methodStr.getValue(), null));
    }
	
    private boolean newIsBetter(Double prediction, Double oldBest) {
		if(this.method.getSecond().getAnnotation(OptimizerOrdering.class).best().equals(OptimizerOrdering.OrderingOption.High)) {
			return prediction > oldBest;
		}else {
			return prediction < oldBest;
		}
    }
	
	
}
