package uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.nd4j.shade.guava.base.Throwables;

import uni_ko.JHPOFramework.DB_Utils.Table_Information;
import uni_ko.JHPOFramework.DB_Utils.Table_Updates;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Numeric_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;

public class Config_Utils {

	public static Integer generate_cases(
			Connection db_con,
			Class<? extends Classifier> classifier,
			HashMap<Integer, Range> configuration_data,
			List<Integer> data_ids,
			Integer opt_id
			) throws Exception {
		
		List<HashMap<Integer, Object>> results = Config_Utils.getAllPossibleCases(classifier, configuration_data, true);

		for(int data_id : data_ids) {	
			for(HashMap<Integer, Object> single_task : results) {
				Table_Updates.registerNewConfiguration(db_con, Config_Utils.to_json(single_task), classifier, data_id, opt_id);
			}
		}
		return results.size();
	}
	
	
	public static List<HashMap<Integer, Object>> getAllPossibleCases(Class<? extends Classifier> classifier, HashMap<Integer, Range> configuration_data, boolean forDBCast) throws InstantiationException, IllegalAccessException{
		List<HashMap<Integer, Object>> results = new ArrayList<>();
		HashMap<Integer, Object> zero_case = new HashMap<Integer, Object>();
		List<Parameter_Communication_Wrapper> classifier_parameters = classifier.newInstance().configurational_parameters();
		// case zero
		int key = 0;
		for(Entry<Integer, Range> entry : configuration_data.entrySet()) {
			if(forDBCast && classifier_parameters.get(key).getClass().equals(Parameter_Communication_Wrapper_Lists.class)) {
				// add null value to enforce type List
				zero_case.put(key, Arrays.asList(entry.getValue().options.get(0), null) );
			}else {
				zero_case.put(key, entry.getValue().options.get(0));
			}
			key ++;
		}
		results = Config_Utils.permutate(zero_case, 0, configuration_data, classifier_parameters, forDBCast);
		return results;
	}
	protected static List<HashMap<Integer, Object>> permutate(
			HashMap<Integer, 
			Object> conf, 
			Integer index,
			HashMap<Integer, Range> configuration_data,
			List<Parameter_Communication_Wrapper> classifier_parameters,
			boolean forDBCast
			){
		if(configuration_data.size() <= index) {
			return  new ArrayList<HashMap<Integer, Object>>(Arrays.asList(conf));
		}
		List<HashMap<Integer, Object>> buffer = new ArrayList<HashMap<Integer,Object>>();
		for(Object o : configuration_data.get(index).options) {
			if(classifier_parameters.get(index).getClass().equals(Parameter_Communication_Wrapper_Lists.class)) {
				// add null value to enforce type List
				if(!forDBCast) {
					conf.put(index, o);
				}else if(o.getClass().equals(Arrays.asList(1).getClass())) {
					List<Object> l = new ArrayList<Object>();
					l.addAll((Collection<? extends Object>) o);
					l.add(null);
					conf.put(index, l);
				}else {
					conf.put(index, new ArrayList<>(Arrays.asList(o, null)));
				}
				
			}else {
				conf.put(index, o);
			}
			buffer.addAll(Config_Utils.permutate((HashMap<Integer, Object>)conf.clone(), index+1, configuration_data, classifier_parameters, forDBCast));
		}
		return buffer;
	}	
	public static String to_json(HashMap<Integer, Object> parameters) throws Exception {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(parameters);
	    oos.close();
	    return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	public static HashMap<Integer, Object> from_json(String s) throws Exception{
	    byte[] data = Base64.getDecoder().decode(s);
	    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
	    HashMap<Integer, Object> o = (HashMap<Integer, Object>) ois.readObject();
	    ois.close();
	    for(Entry<Integer, Object> entry : o.entrySet()) {
	    	if( entry.getValue().getClass().equals(Arrays.asList(1).getClass()) ||
	    		entry.getValue().getClass().equals(ArrayList.class)) {
	    		// remove null values - used to enforce type List
	    		List<Object> lo = new ArrayList<Object>((Collection<? extends Object>)entry.getValue());
	    		if(lo.get(0).getClass().equals(ArrayList.class)) {
	    			entry.setValue(lo.get(0));
	    		}else {
	    			while(lo.contains(null)) {
		    			lo.remove(null);
		    		}
		    		entry.setValue(lo);
	    		}
	    	}
	    }
	    
	    return (HashMap<Integer, Object>) o;
	}

}
