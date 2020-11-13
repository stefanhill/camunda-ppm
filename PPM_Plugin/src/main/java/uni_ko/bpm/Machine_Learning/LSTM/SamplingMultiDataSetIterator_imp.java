package uni_ko.bpm.Machine_Learning.LSTM;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.MultiNormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Data_Management.Data_Set;

public class SamplingMultiDataSetIterator_imp implements MultiDataSetIterator {

//	private MultiDataSet sampleFrom;
    private int batchSize;
    private int totalNumberSamples;
    private int numTimesSampled = 0;
    private MultiDataSetPreProcessor preProcessor;

    private int unique_task_count = 0;
    /**
     * @param sampleFrom         the dataset to sample from
     * @param batchSize          the batch size to sample
     * @param totalNumberSamples the sample size
     * @throws Exception 
     */
    public SamplingMultiDataSetIterator_imp(Data_Set data_set, List<Field_Config> field_config,  int batchSize, int unique_task_count) throws Exception {
    	super();
    	
    	this.field_config = field_config;
    	this.unique_task_count = unique_task_count;
    	this.create_from_Data_Set(data_set);
    	this.batchSize = batchSize;
        this.totalNumberSamples = data_set.get_flow_count();
        
        
        this.buffered_feature_arr = this.feature_arr;
        this.buffered_labels_arr = this.labels_arr;
        
        MultiNormalizerMinMaxScaler mnmms = new MultiNormalizerMinMaxScaler(0.0, 1.0);
        mnmms.fitLabel(true);
        mnmms.fit(this.to_MultiDataSet());
        this.preProcessor = mnmms;
    }
    public SamplingMultiDataSetIterator_imp(Data_Set data_set, List<Field_Config> field_config,  int batchSize, int unique_task_count, MultiDataSetPreProcessor preProcessor) throws Exception {
    	super();
    	
    	this.field_config = field_config;
    	this.unique_task_count = unique_task_count;
    	this.create_from_Data_Set(data_set);
    	this.batchSize = batchSize;
        this.totalNumberSamples = data_set.get_flow_count();
        
        
        this.buffered_feature_arr = this.feature_arr;
        this.buffered_labels_arr = this.labels_arr;
        
        this.preProcessor = preProcessor;
    }


    @Override
    public boolean hasNext() {
        return (numTimesSampled+batchSize) <= totalNumberSamples;
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
    	// in-memory prefetching is simpler and has less function calls
        return false;
    }

    @Override
    public void reset() {
        numTimesSampled = 0;
        this.buffered_feature_arr = this.feature_arr;
        this.buffered_labels_arr = this.labels_arr;
    }


	@Override
	public MultiDataSet next() {
        MultiDataSet ret = this.sample(this.batchSize);
        if(ret == null && this.prefetched_data.size() > 0) {
            ret = this.prefetched_data.get(0).getKey();
            this.prefetched_data.remove(0);
        }
        if (preProcessor != null) {
        	preProcessor.preProcess(ret);
        }
        return ret;
	}


	@Override
	public MultiDataSet next(int num) {
		MultiDataSet ret = this.sample(num);
        return ret;
	}



	@Override
	public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
		this.preProcessor = preProcessor;
		
	}


	@Override
	public MultiDataSetPreProcessor getPreProcessor() {
		return this.preProcessor;
	}
    

	public List<double[][][]> feature_arr = new ArrayList<double[][][]>();
	public List<double[][][]> labels_arr = new ArrayList<double[][][]>();
    
	public List<double[][][]> buffered_feature_arr = new ArrayList<double[][][]>();
	public List<double[][][]> buffered_labels_arr = new ArrayList<double[][][]>();
	
    private int d1 = 0;
    private int d3 = 0;
    

    private List<List<List<Double>>> data;
    private List<Field_Config> field_config;
    // <String-fieldnames, one_hot_in, one_hot_out>
    public void create_from_Data_Set(Data_Set data_set) throws Exception {
//    	for(Field_Config fc : field_config) {
//    		this.fields.add(fc.field_name);
//    	}
    	
		this.d1 = data_set.get_flow_count();
		this.d3 = data_set.get_Longest_Path()-1;
		
    	this.load_data(data_set, field_config);
    	
    	for(int field_index = 0; field_index < field_config.size(); field_index++) {
    		Field_Config fcl = field_config.get(field_index);
    		Pair<double[][][], double[][][]> one_set = 
	    				this.create_by_field(	field_config.get(field_index),
	    										field_index, 
	    										data_set
	    									);

	    	if(field_config.get(field_index).has_input) {
	    		this.feature_arr.add(one_set.getFirst());
	    	}
	    	if(field_config.get(field_index).has_output) {
	    		this.labels_arr.add(one_set.getSecond());
    		}
    	}
    	
    }
    
    private void load_data(Data_Set data_set, List<Field_Config> field_config) throws Exception{
    	this.data = new ArrayList<List<List<Double>>>();
		for(Field_Config fc : field_config) {
//			List<List<Object>> oooo = data_set.<Object>get_set_parameter(fc.field_name);
//			List<List<Double>> buffer = new ArrayList<List<Double>>();
//			for(List<Object> oo : oooo) {
//				List<Double> buf = new ArrayList<Double>();
//				for(Object o : oo) {
//					buf.add(Double.valueOf(o.toString()));
//				}
//				buffer.add(buf);
//			}
			this.data.add(
			data_set.<Object>get_set_parameter(fc.field_name)
					.stream().map(
							l -> l.stream().map(o -> Double.valueOf(o.toString())).collect(Collectors.toList())
					).collect(Collectors.toList())
			);
			
//			this.data.add(buffer);
		//	this.data.add(data_set.<Double>get_set_parameter(fc.field_name));
		}
    }
    
    public static Triple<List<String>, List<Boolean>, List<Boolean>> reshape(List<Triple<String, Boolean, Boolean>> fields){
    	List<String> left = new ArrayList<String>();
    	List<Boolean> middle = new ArrayList<Boolean>();
    	List<Boolean> right = new ArrayList<Boolean>();
    	
    	for(Triple<String, Boolean, Boolean> t : fields) {
    		left.add(t.getLeft());
    		middle.add(t.getMiddle());
    		right.add(t.getRight());
    	}

    	return new Triple(left, middle, right);
    }
    
 // <feature, label>
    private Pair<double[][][], double[][][]> create_by_field(Field_Config field_config, int field_index, Data_Set data_set) {		
//    	double[][][] f_arr = (field_config.one_hot_in)  ? new double[d1][data_set.get_unique_Task_Name().values().size()][d3] : new double[d1][1][d3];
//    	double[][][] l_arr = (field_config.one_hot_out) ? new double[d1][data_set.get_unique_Task_Name().values().size()][d3] : new double[d1][1][d3];
    	
    	double[][][] f_arr = (field_config.one_hot_in)  ? new double[d1][this.unique_task_count][d3] : new double[d1][1][d3];
    	double[][][] l_arr = (field_config.one_hot_out) ? new double[d1][this.unique_task_count][d3] : new double[d1][1][d3];
    	
    	for(int set = 0; set < this.d1; set++){
			for(int flow = 0; flow < this.d3; flow++) {
				Pair<Integer, Integer> flow_modification = field_config.flow_index;
				// in
				int flow_index;
				if(field_config.has_input) {
					flow_index = (flow + flow_modification.getFirst());
					if(flow_index >= 0 && flow_index < data.get(field_index).get(set).size()/*this.d3*/) {
						if(field_config.one_hot_in) {
							switch (field_config.field_name) {
							case "task_uid":
								f_arr[set][data.get(field_index).get(set).get(flow_index).intValue()][flow] = 1.0;
								break;
							case "duration":
								f_arr[set][data.get(0).get(set).get(flow_index).intValue()][flow] = data.get(field_index).get(set).get(flow_index);
								break;
							}
						}else {
							f_arr[set][0][flow] = data.get(field_index).get(set).get(flow_index);
						}
					}
				}
				// out
				if(field_config.has_output) {
					flow_index = (flow + flow_modification.getSecond());
					if(flow_index >= 0 && flow_index < data.get(field_index).get(set).size()/*this.d3*/) {
						if(field_config.one_hot_out) {
							switch (field_config.field_name) {
							case "task_uid":
								int i = data.get(field_index).get(set).get(flow_index).intValue();
								l_arr[set][data.get(field_index).get(set).get(flow_index).intValue()][flow] = 1;
								break;
							case "duration":
								l_arr[set][data.get(0).get(set).get(flow_index).intValue()][flow] = Double.valueOf(data.get(field_index).get(set).get(flow_index).toString());
								break;
							}
						}else {
							l_arr[set][0][flow] = Double.valueOf(data.get(field_index).get(set).get(flow_index).toString());
						}
					}
				}
			}
		}
    	return new Pair(f_arr, l_arr);
    }

    
 
    

  
    

    

    
    // Threadsafety required
    private Vector<Pair<MultiDataSet, Integer>> prefetched_data = new Vector<>();
    
    public MultiDataSet sample(int numSamples) {
    	MultiDataSet mds = null;
    	int position = 0;
    	if(prefetched_data.size() > 0) {
    		for(position = 0; position < prefetched_data.size(); position++) {
    			if(prefetched_data.get(position).getValue() == numSamples) {
    				mds = prefetched_data.get(position).getKey();
    				prefetched_data.remove(position);
    				break;
    			}
    		}
    		if(mds == null) {
    			new Thread( () -> this.async_sampler(numSamples)).start();
    			return this.async_sampler(numSamples);
    		}	
    	}else {
    		new Thread( () -> this.async_sampler(numSamples)).start();
			return this.async_sampler(numSamples);
    	}
    	new Thread( () -> this.async_sampler(numSamples)).start();
    	return mds;

    }
    
    
    private MultiDataSet async_sampler(int numSamples) {
    	// do not allow to much prefetched data
    	if(this.prefetched_data.size() > 10) {
    		return null;
    	}
    	INDArray[] mutli_features 	= new INDArray[this.feature_arr.size()];
    	INDArray[] mutli_labels 	= new INDArray[this.labels_arr.size()];
    	
    	for(int i = 0; i < this.buffered_feature_arr.size(); i++) {
    		double[][][] feature_arr_buf = new double[numSamples][this.buffered_feature_arr.get(i)[0].length][d3];
    		int rest = buffered_feature_arr.get(i).length - numTimesSampled;
    		if(buffered_feature_arr.get(i).length >= numTimesSampled+numSamples) {
	    		for(int s = 0; s < numSamples; s++) {
	    			feature_arr_buf[s] = buffered_feature_arr.get(i)[numTimesSampled+s];
	    		}
    		}else {
    			return null;
    		}
    		mutli_features[i] = Nd4j.create( feature_arr_buf );
    	}
		
    	for(int i = 0; i < this.buffered_labels_arr.size(); i++) {
    		
    		double[][][] labels_arr_buf = new double[numSamples][this.buffered_labels_arr.get(i)[0].length][d3];
    		int rest = buffered_labels_arr.get(i).length - numTimesSampled;
    		if(buffered_labels_arr.get(i).length >= numTimesSampled+numSamples) {
	    		for(int s = 0; s < numSamples; s++) {
	    			labels_arr_buf[s] = buffered_labels_arr.get(i)[numTimesSampled+s];
	    		}
    		}else {
    			return null;
    		}
    		mutli_labels[i] = Nd4j.create( labels_arr_buf );
    	}
    	
    	
        numTimesSampled += numSamples;
        
        MultiDataSet mds = new org.nd4j.linalg.dataset.MultiDataSet(mutli_features, mutli_labels);
        this.prefetched_data.add(new Pair<>(mds, numSamples));
        return mds;
    }
    
    public MultiDataSet to_MultiDataSet() {
    	
    	INDArray[] mutli_features 	= new INDArray[this.feature_arr.size()];
		INDArray[] mutli_labels 	= new INDArray[this.labels_arr.size()];
		
		for(int i = 0; i < this.feature_arr.size(); i++) {
			mutli_features[i] = Nd4j.create( this.feature_arr.get(i) );
		}
		
		for(int i = 0; i < this.labels_arr.size(); i++) {
			mutli_labels[i] = Nd4j.create( this.labels_arr.get(i) );
		}
		
		return new org.nd4j.linalg.dataset.MultiDataSet(mutli_features, mutli_labels);
    }
    public int size() {
    	return this.totalNumberSamples;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
