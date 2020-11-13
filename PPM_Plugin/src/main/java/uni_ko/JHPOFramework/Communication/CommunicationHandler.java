package uni_ko.JHPOFramework.Communication;

import org.apache.commons.io.FileUtils;
import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Config_Utils;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor.Pause_Type;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveOption;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.Configuration;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.ConfigurationBuilder;
import uni_ko.JHPOFramework.SimulationEnvironment.Data.Data;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Data_Management.Data_Reader.Data_Reader;
import uni_ko.bpm.Data_Management.Data_Reader.History_Reader;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Reflections.ReflectionReads;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CommunicationHandler {
	private HashMap<String, Executor> activeExecutors = new HashMap<String, Executor>();

	private String processDefinitionId;
	private Table_Read tr = new Table_Read();
	private ExecutorService asyncRequests = Executors.newFixedThreadPool(CommunicationData.maximum_async_executions_per_processDefinitionId);
	
	
	// <ProjectName, Status, numberOfIterations>
	public List<Triple<String, String, Integer>> getAllSimulations() throws Exception {
		List<Triple<String, String, Integer>> result = new ArrayList<>();
		File defaultLocation = new File(this.getDefaultPath());
		if(defaultLocation.exists()) {
			for(File folder : Objects.requireNonNull(defaultLocation.listFiles())) {
				if(folder.isDirectory()) {
					String projectName = folder.getName();
					try {
						Pair<String, Integer> status = this.getExecutor(projectName).getStatus();
						result.add(new Triple<>(projectName, status.getFirst(), status.getSecond()));
					}
					catch (Exception e) {
						result.add(new Triple<>(projectName, "waiting", -1));
					}
				}
			}
		}
		/*
		for(Entry<String, Executor> exe : this.activeExecutors.entrySet()) {
			Pair<Integer, Integer> status = exe.getValue().getStatus();
			if(result.stream().noneMatch(x -> x.getFirst().equals(exe.getValue().projectName))) {
				result.add(new Triple<String, Integer, Integer>(exe.getValue().projectName, status.getFirst(), status.getSecond()));
			}
		}*/
		return result;
	}

	
	public Map<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> getConfiguration(String projectName) throws Exception {
		return this.getExecutor(projectName).getConfiguration();
	}
	
	public List<ResultWrapper> getSavedResults(String projectName) throws Exception {
		List<ResultWrapper> result = new ArrayList<ResultWrapper>();
		Executor exe = this.getExecutor(projectName);	
		// <runId, <configuration, runTime>>
		HashMap<Integer, Pair<String, Integer>> prefetch = new HashMap<Integer, Pair<String,Integer>>();
		ResultSet rs = Table_Read.readConfigurationAndRuntime(exe.getDBCon());
		while(rs.next()) {
			prefetch.put(rs.getInt(1), new Pair<String, Integer>(rs.getString(2), rs.getInt(3)));
		}
		for(SaveOption so : exe.getSaveDefinition().getSaveOptions()) {
			for(Entry<Integer, Pair<File, Double>> entry : so.savedClassifier.entrySet()) {
				Pair<String, Integer> pre = prefetch.get(entry.getKey());

				Classifier classifier = ReflectionReads.deserializeClassifier(entry.getValue().getKey(), (entry.getValue().getKey().getName().split("\\$"))[0]);
				List<Parameter_Communication_Wrapper> lpcw = classifier.configurational_parameters();
				HashMap<String, Object> configuration = new HashMap<String, Object>();
				for(Entry<Integer, Object> confEntry : Config_Utils.from_json(pre.getKey()).entrySet()) {
					for(Parameter_Communication_Wrapper pcw : lpcw) {
						if(pcw.parameter_id.equals(confEntry.getKey())) {
							configuration.put(pcw.text, confEntry.getValue());
						}
					}
				}
				
				result.add(new ResultWrapper(	classifier.getClass().getSimpleName(),
												entry.getKey(),
												new Pair<String, Double>(so.metric.getKey(), entry.getValue().getValue()), 
												configuration, 
												pre.getValue()
												)
						);
			}
		}
		return result;
	}

	public List<HashMap<String, String>> getTop(String projectName, Integer number) throws Exception {
		Executor exe = this.getExecutor(projectName);
		return Table_Read.readTopResults(exe.getDBCon(), number, exe.getMetrices().entrySet().iterator().next().getKey());
	}

	public void createClassifier(Integer id, String projectName, String givenName, String author, boolean trained) throws Exception {
		Frontend_Request fr = Frontend_Communication_Data.get_frh(URLDecoder.decode(this.processDefinitionId, Frontend_Communication_Data.serialization_characterset.toString()));
		Executor exe = this.getExecutor(projectName);
		if(trained) {
			Classifier classifier = Table_Read.getClassifierByID(exe, id, givenName, author);
			fr.add_Classifier(classifier);
		}else {
			
			Pair<String, HashMap<Integer, Object>> data = Table_Read.getClassifierDataByID(exe.getDBCon(), id);
			fr.create_classifier(givenName, data.getKey(), data.getValue(), author);
		}
	}

	public void createSimulation(
			String projectName, 
			Pair<String, HashMap<Integer, Object>> optimizerConfig, 	// Pair<OptimizerName, Config aka ParamConwrapper>
			String metricName, 					
			HashMap<String, HashMap<Integer, Range>> classifierConfig, // 
			Integer saveNumber,											// save best X
			Double trainingRatio,
			InputStream data_stream
			) throws Exception {
		Data_Set dataSet = Data_Set.input_stream2Data_Set(data_stream, this.processDefinitionId);
		this.createSimulation(projectName, optimizerConfig, metricName, classifierConfig, saveNumber, trainingRatio, dataSet);
	}
	public void createSimulation(
			String projectName, 
			Pair<String, HashMap<Integer, Object>> optimizerConfig,
			String metricName, 					
			HashMap<String, HashMap<Integer, Range>> classifierConfig,  
			Integer saveNumber,
			Double trainingRatio,
			Date startDate, 
			Date finishedDate
			) throws Exception {
		Data_Reader dr = new History_Reader(URLDecoder.decode(this.processDefinitionId, Frontend_Communication_Data.serialization_characterset.toString()), startDate, finishedDate);
		Data_Set dataSet = dr.get_instance_flows();
		this.createSimulation(projectName, optimizerConfig, metricName, classifierConfig, saveNumber, trainingRatio, dataSet);
	}
	public void createSimulation(
			String projectName, 
			Pair<String, HashMap<Integer, Object>> optimizerConfig,
			String metricName, 					
			HashMap<String, HashMap<Integer, Range>> classifierConfig,  
			Integer saveNumber,
			Double trainingRatio
			) throws Exception {
		Data_Reader dr = new History_Reader(URLDecoder.decode(this.processDefinitionId, Frontend_Communication_Data.serialization_characterset.toString()));
		Data_Set dataSet = dr.get_instance_flows();
		this.createSimulation(projectName, optimizerConfig, metricName, classifierConfig, saveNumber, trainingRatio, dataSet);
	}
	public void createSimulation(
			String projectName, 
			Pair<String, HashMap<Integer, Object>> optimizerConfig, 
			String metricName, 					
			HashMap<String, HashMap<Integer, Range>> classifierConfig, 
			Integer saveNumber,
			Double trainingRatio,
			Data_Set dataSet
			) throws Exception {
		Data data = new Data().builder().addData(dataSet, trainingRatio, "DataSet");
		
		Pair<String, Method> onMetric = null;
		MetricConfiguration metricConfiguration = new MetricConfiguration();
		for(Method method : ReflectionReads.getMetricMethods()) {
			if(method.getName().equals(metricName)) {
				onMetric = new Pair<String, Method>(metricName, method);
			}
			metricConfiguration.addMetric(method.getName(), method);
		}
		
		

		
		ConfigurationBuilder configurationBuilder = new Configuration().builder();
		for(Entry<String, HashMap<Integer, Range>> entry : classifierConfig.entrySet()) {
			for(Class<? extends Classifier> cl : ReflectionReads.getPossibleClassifierClasses()) {
				if(cl.getSimpleName().equals(entry.getKey())) {
					configurationBuilder.add_classifier(cl).add_parameter(entry.getValue());
				}
			}
		}

		SaveDefinition saveDefinition = new SaveDefinition().builder()
											.saveTopXClassifer(saveNumber, this.getProjektPath(projectName), onMetric)
											.build();
		

		OptimizationAlgorithm optimizationAlgorithm = null;
		for(Class<? extends OptimizationAlgorithm> oC : uni_ko.JHPOFramework.Reflections.ReflectionReads.getPossibleOptimizationClasses()) {
			if(optimizerConfig.getKey().equals(oC.getSimpleName())) {
				optimizationAlgorithm = oC.newInstance();
				optimizationAlgorithm.set_configurational_parameters(optimizerConfig.getValue());
				break;
			}
		}
		
		// add saveNumber - metricName
		Executor executor = new Executor(
				projectName, 
				this.getProjektPath(projectName), 
				data, 
				metricConfiguration, 
				configurationBuilder.build(), 
				saveDefinition, 
				optimizationAlgorithm
				);
		this.activeExecutors.put(projectName, executor);
		
		executor.start_execution();	
	}

	public List<String> getAllClassifier() throws Exception {
		List<String> ret = new ArrayList<String>();
		for(Class<? extends Classifier> classifier : ReflectionReads.getPossibleClassifierClasses()) {
			if(classifier.newInstance().is_creatable()) {
				ret.add(classifier.getSimpleName());
			}
		}
		return ret;
	}	
	public List<Range> getNeededRanges(String classifierType, Double lower, Double upper) throws Exception{
		Classifier classifier = null;
		for(Class<? extends Classifier> cC : ReflectionReads.getPossibleClassifierClasses()) {
			if(cC.getSimpleName().contentEquals(classifierType)) {
				classifier = cC.newInstance();
				break;
			}
		}
		List<Range> neededRanges = new ArrayList<Range>();
		for(Parameter_Communication_Wrapper pcw : classifier.configurational_parameters()) {
			neededRanges.add(pcw.toRange(lower, upper));
		}
		return neededRanges;
	}

	public List<Range> getNeededRanges(String classifierType) throws Exception {
		return this.getNeededRanges(classifierType, 0.5, 1.5);
	}
	
	public List<String> getAllMetrices() throws Exception{
		return ReflectionReads.getMetricMethodsAsString();
	}
	
	public HashMap<String, List<Parameter_Communication_Wrapper>> getAllOptimizer() throws Exception{
		HashMap<String, List<Parameter_Communication_Wrapper>> ret = new HashMap<String, List<Parameter_Communication_Wrapper>>();
		for(Class<? extends OptimizationAlgorithm> oC : uni_ko.JHPOFramework.Reflections.ReflectionReads.getPossibleOptimizationClasses()) {
			OptimizationAlgorithm oa = oC.newInstance();
			ret.put(oC.getSimpleName(), oa.configurational_parameters());
		}
		return ret;
	}
	
	public void pauseSimulation(String projectName) {	
		if(this.activeExecutors.containsKey(projectName)) {
			Executor exe = this.activeExecutors.get(projectName);
			exe.pause_execution(Pause_Type.Instant);
			this.activeExecutors.remove(exe);
		}
	}
	public void resumeSimulation(String projectName) throws Exception {	
		Executor exe = Executor.Executor(projectName, this.getProjektPath(projectName));
		exe.start_execution();
	}
	
	
	public void deleteSimulation(String projectName) throws Exception {
		Executor exe = this.getExecutor(projectName);
		exe.removeDBCon();
		String path = this.getProjektPath(projectName);
		File folder = new File(path.substring(0, path.length()-1));
		FileUtils.deleteDirectory(folder);
		//folder.delete();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public void set_PDI(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}
	private String getDefaultPath() {
		return CommunicationData.defaultPath + processDefinitionId +File.separator;
	}
	private String getProjektPath(String projectName) {
		return this.getDefaultPath() + projectName + File.separator;
	}
	private Executor getExecutor(String projectName) throws Exception {
		if(this.activeExecutors.containsKey(projectName)) {
			return this.activeExecutors.get(projectName);
		}else {
			String projectPath = this.getProjektPath(projectName);
			if(new File(projectPath).exists()) {
				return Executor.Executor(projectName, this.getProjektPath(projectName));
			}
		}
		return null;
	}

	public Future<Boolean> asyc_Request (Runnable request){
		return (Future<Boolean>) this.asyncRequests.submit(request);
	}



}
