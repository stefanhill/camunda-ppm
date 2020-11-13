package uni_ko.bpm.Machine_Learning.Merged_Classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Classifier_Exception;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.MetricImpl.MergedMetric;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.PredictionType;

public class Merged_Classifier extends Classifier {
    public List<Classifier> classifiers = new ArrayList<Classifier>();

    @Override
    public Metric train(Data_Set training_set, Data_Set test_set) throws Exception {
        List<Metric> metrices = new ArrayList<Metric>();
        for (Classifier c : this.classifiers) {
            metrices.add(c.train(training_set, test_set));
        }
        return new MergedMetric(metrices);
    }

    @Override
    public List<Parameter_Communication_Wrapper> configurational_parameters() {
        // Should not be outside accessible
        return new ArrayList<>();
    }

    @Override
    public void set_configurational_parameters(HashMap<Integer, Object> parameters) {
        // TODO Auto-generated method stub
        this.classifiers = (List<Classifier>) parameters.get(0);
    }

    @Override
    public List<Classification> evaluate(Data_Set data_set) throws Exception {
        List<List<Classification>> classicifation = new ArrayList<>();
        for (Classifier c : this.classifiers) {
            classicifation.add(c.evaluate(data_set));
        }
        return this.merge_classifications(classicifation);
    }
    
    protected List<Classification> merge_classifications(List<List<Classification>> classification_list) {
    	List<Classification> ret_classification = new ArrayList<>();
    	HashMap<PredictionType, List<Classification>> cleand_list = new HashMap<>();
    	for(List<Classification> lc : classification_list) {
    		for(Classification c : lc) {
    			if(cleand_list.containsKey(c.type)) {
    				List<Classification> buffer = new ArrayList<>();
    				buffer.addAll(cleand_list.get(c.type));
    				buffer.add(c);
    				cleand_list.replace(c.type, buffer);
    			}else {
    				cleand_list.put(c.type, Arrays.asList(c));
    			}
    		}
    	}
    	for(Entry<PredictionType, List<Classification>> entry : cleand_list.entrySet()) {
    		HashMap<String, Float> evals = new HashMap<>();
    		HashMap<String, Integer> counter = new HashMap<>();
    		for(Classification classification : entry.getValue()) {
    			for(Entry<String, Float> single_entry : classification.evals.entrySet()) {
    				if(evals.containsKey(single_entry.getKey())) {
    					evals.replace(single_entry.getKey(), (evals.get(single_entry.getKey()) + single_entry.getValue()) );
    					counter.replace(single_entry.getKey(), (counter.get(single_entry.getKey()) + 1) );
    				}else {
    					evals.put(single_entry.getKey(), single_entry.getValue() );
    					counter.put(single_entry.getKey(), 1 );
    				}
    			}
    		}
    		for(Entry<String, Integer> single_entry : counter.entrySet()) {
    			evals.replace(single_entry.getKey(), ( evals.get(single_entry.getKey()) / single_entry.getValue() ) );
    		}
    		ret_classification.add(new Classification(evals, entry.getKey()));
    	}
    	return ret_classification;
    }

    @Override
    public List<PredictionType> get_prediction_type() {
        List<PredictionType> types = new ArrayList<>();
        for (Classifier c : this.classifiers) {
            types.addAll(c.get_prediction_type().stream()
                    .filter(pt -> !types.contains(pt)).collect(Collectors.toList()));
        }
        return types;
    }

    @Override
    public boolean is_creatable() {
        return false;
    }

    @Override
    public boolean is_trainable() {
        return false;
    }

}
