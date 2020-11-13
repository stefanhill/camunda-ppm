package uni_ko.JHPOFramework.SimulationEnvironment.Optimizer;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.DB_Utils.DefaultStringSerialization;
import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.DB_Utils.Table_Updates;
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


public abstract class OptimizationAlgorithm extends DefaultStringSerialization implements Serializable, Cloneable{
	public Integer optimizerID;
	
	protected Pair<List<Integer>, List<Range>> configuration;
	public Integer dataID;
	public Class<? extends Classifier> classType;
	protected SaveDefinition saveDefinition;
	protected MetricConfiguration metricConfiguration;
	protected transient Connection db_con;
	protected List<Integer> tbmIDs;

	protected Integer permutatedConfigurationsCount = 1;
	protected int maximumGenerations;
	
	public OptimizationAlgorithm() {}
	
	public void initAlgorithm(
			Integer optimizerID,
			Pair<List<Integer>, List<Range>> configuration,
			Integer dataID,
			Class<? extends Classifier> classType,
			SaveDefinition saveDefinition,
			MetricConfiguration metricConfiguration,
			Connection db_con,
			List<Integer> tbmIDs
			) throws Exception{
		this.optimizerID = optimizerID;
		this.configuration = configuration;
		this.dataID = dataID;
		this.classType = classType;
		this.saveDefinition = saveDefinition;
		this.metricConfiguration = metricConfiguration;
		this.db_con = db_con;
		this.tbmIDs = tbmIDs;
		
		for(Range r : configuration.getValue()) {
			this.permutatedConfigurationsCount *= r.options.size();
		}
		maximumGenerations = (maximumGenerations >= this.permutatedConfigurationsCount) ? this.permutatedConfigurationsCount : maximumGenerations;
		
	}
	
	public abstract boolean hasNext() throws Exception;
	public abstract Pair<Integer, HashMap<Integer, Object>> nextConfigurationString(Metric metric, HashMap<Integer, Object> configuration) throws Exception;
	
	public abstract Integer getMaxCases();
    public OptimizationAlgorithm clone() throws CloneNotSupportedException {
        return (OptimizationAlgorithm) super.clone();
    }
    
	public abstract List<Parameter_Communication_Wrapper> configurational_parameters() throws Exception;
	public abstract void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception;
	
	public void setDBConn(Connection db_con) {
		this.db_con = db_con;
	}
	public SaveDefinition getSaveDefinition() {
		return this.saveDefinition;
	}
}
