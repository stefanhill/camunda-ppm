package uni_ko.JHPOFramework.SimulationEnvironment.Optimizer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.management.InvalidAttributeValueException;

import java.util.Map.Entry;
import java.util.stream.Collectors;

import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.DB_Utils.Table_Updates;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Config_Utils;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.CheckMethods;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;
import uni_ko.bpm.Reflections.ReflectionReads;
import weka.core.Attribute;
import weka.core.Instances;

public class RandomOptimizer extends OptimizationAlgorithm{
	
	private List<HashMap<Integer, Object>>  possibleConfigurations = null;
	private int currentGeneration = 0;
	
	private double goal = 0.0;
	private double metricValue = 0.0;
	protected transient Pair<String, Method> method;
	
	public RandomOptimizer() {}
	public RandomOptimizer(int maximumGenerations, double goal, Method method) throws InvalidAttributeValueException {
		this.maximumGenerations = maximumGenerations;
		this.goal = goal;
		CheckMethods.checkMethod(method);
		this.method = new Pair<String, Method>(method.getName(), method);
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

		HashMap<Integer, Range> configuration_data = new HashMap<Integer, Range>();
		for(int index = 0; index < this.configuration.getKey().size(); index++) {
			configuration_data.put(this.configuration.getKey().get(index), this.configuration.getValue().get(index));
		}
		this.possibleConfigurations = Config_Utils.getAllPossibleCases(this.classType, configuration_data, false);
		
		saveDefinition.setClassifierDistribution(this.db_con, this.maximumGenerations, this.classType);
		saveDefinition.validate(metricConfiguration.relevant_metric_methods);		
	}
	
	
	@Override
	public boolean hasNext() throws Exception {
		return this.maximumGenerations > currentGeneration &&
				this.metricValue < goal;
	}

	@Override
	public Pair<Integer, HashMap<Integer, Object>> nextConfigurationString(
			Metric metric,
			HashMap<Integer, Object> configuration
			) throws Exception {
		if(metric != null) {
			this.metricValue = (double) this.method.getValue().invoke(metric, null);
		}
		int index = new Random().nextInt(this.possibleConfigurations.size());
		HashMap<Integer, Object> nextConfiguration = this.possibleConfigurations.get(index);
		this.possibleConfigurations.remove(index);
		this.currentGeneration ++;
		return new Pair<Integer, HashMap<Integer, Object>>(this.saveToDB(Config_Utils.to_json(nextConfiguration)), nextConfiguration);
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
				"Simulationsize", 
				"The maximum number of simulations done.", 
				false,
				this.maximumGenerations,
				Integer.class, 
				20)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Double>(
				1, 
				"Goal", 
				"When the given metric reaches this, the execution will be stopped.", 
				false,
				this.goal,
				Double.class, 
				99.0)
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
		
		return parameters;
	}

	@Override
	public void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception {
		this.maximumGenerations = (int) parameters.get(0);
		this.goal = (double) parameters.get(1);
		this.method = new Pair<String, Method>(((List<String>) parameters.get(2)).get(0), ReflectionReads.getMetricMethod(((List<String>) parameters.get(2)).get(0)));
		
	}
	
	private Integer saveToDB(String configuration) throws Exception {
		return Table_Updates.registerNewConfiguration(this.db_con, configuration, this.classType, this.dataID, this.optimizerID).intValue();
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
	

}
