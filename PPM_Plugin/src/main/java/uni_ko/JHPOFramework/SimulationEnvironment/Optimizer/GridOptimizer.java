package uni_ko.JHPOFramework.SimulationEnvironment.Optimizer;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uni_ko.JHPOFramework.DB_Utils.Table_Management;
import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.DB_Utils.Table_Updates;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Config_Utils;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;

public class GridOptimizer extends OptimizationAlgorithm{

	private Pair<Integer, HashMap<Integer, Object>> nextConfiguration = null;
	private Integer cases;
	public GridOptimizer() throws Exception { }

	@Override
	public void initAlgorithm(
			Integer optimizerID,
			Pair<List<Integer>, List<Range>> configuration, 
			Integer dataID,
			Class<? extends Classifier> classType, 
			SaveDefinition saveDefinition,
			MetricConfiguration metricConfiguration, 
			Connection db_con, 
			List<Integer> tbmIDs
			) throws Exception {
		
		super.initAlgorithm(optimizerID, configuration, dataID, classType, saveDefinition, metricConfiguration, db_con, tbmIDs);
		if(configuration.getKey().size() != configuration.getValue().size()) {
			throw new Exception("Length of parameters and length if ranges has to be the same!");
		}else {
			HashMap<Integer, Range> config_data = new HashMap<Integer, Range>();
			for(int index = 0; index < configuration.getKey().size(); index++) {
				config_data.put(configuration.getKey().get(index), configuration.getValue().get(index));
			}
			
			this.cases = Config_Utils.generate_cases(this.db_con, this.classType, config_data, tbmIDs, this.optimizerID);
		}
		// update SaveDefinitions
		if(!saveDefinition.isSet()) {
			saveDefinition.setClassifierDistribution(this.db_con, this.cases, classType);
			saveDefinition.validate(metricConfiguration.relevant_metric_methods);
		}
	}
	
	@Override
	public boolean hasNext() throws Exception {
		this.nextConfiguration = configurationRead();
		return (nextConfiguration.getValue() != null);
	}

	@Override
	public Pair<Integer, HashMap<Integer, Object>> nextConfigurationString(Metric metric, HashMap<Integer, Object> configuration) throws Exception {
		return this.nextConfiguration;
	}
	
	private Pair<Integer, HashMap<Integer, Object>> configurationRead() throws Exception {
		ResultSet result = Table_Read.readNextUnfinishedTask(this.db_con, this.optimizerID);
		while(result.next()) {
	        Integer run_id 			= result.getInt(1);
	        Integer data_id 		= result.getInt(2);
	        Integer opt_id 			= result.getInt(3);
	        String configuration_str= result.getString(4);
	        String simple_name 		= result.getString(5);
	        boolean is_finished 	= result.getBoolean(6); 
	    
	        return new Pair<Integer, HashMap<Integer,Object>>(run_id, Config_Utils.from_json(configuration_str));
		}
		return new Pair<Integer, HashMap<Integer,Object>>(null, null);
	}

	@Override
	public Integer getMaxCases() {
		return this.cases;
	}

	
	@Override
	public List<Parameter_Communication_Wrapper> configurational_parameters() throws Exception{
		return new ArrayList<Parameter_Communication_Wrapper>();
	}
	@Override
	public void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception {}



}
