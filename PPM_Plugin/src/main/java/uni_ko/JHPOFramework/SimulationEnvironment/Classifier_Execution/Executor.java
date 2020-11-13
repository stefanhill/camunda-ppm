package uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang3.SerializationUtils;
import org.nd4j.nativeblas.Nd4jCpu.boolean_and;

import uni_ko.JHPOFramework.DB_Utils.Table_Information;
import uni_ko.JHPOFramework.DB_Utils.Table_Management;
import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.DB_Utils.Table_Updates;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.Configuration;
import uni_ko.JHPOFramework.SimulationEnvironment.Data.Data;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import weka.clusterers.Cobweb;

public class Executor implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2273486613678148832L;
	public boolean isActive = false;
	public int iterationCount = 0;
	public String projectName;
	// <Data - per DataSet
	// <<per Class
//	public transient List<List<Pair<OptimizationAlgorithm, Boolean>>> searchAlgorithms = new ArrayList<List<Pair<OptimizationAlgorithm, Boolean>>>();
	protected transient Connection db_con;
	
	private String db_path;
	private String project_path;

	protected MetricConfiguration metricConfiguration;
	protected SaveDefinition saveDefinition;
	private HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations;
	protected HashMap<String, Class<? extends Classifier>> registered_classes;
	
	public transient ExecutorService classifier_thread = this.initExecutorService();
	
	public Executor(
			String project_name,
			String project_path, 
			Data data, 
			MetricConfiguration metricConfiguration,
			Configuration configuration,
			SaveDefinition saveDefinition,
			OptimizationAlgorithm optimizer
			) throws Exception {
		
		this.init(project_name, project_path, data, metricConfiguration, configuration.get_conf_raw(), saveDefinition, optimizer);
	}
	public Executor(
			String project_name,
			String project_path, 
			Data data, 
			MetricConfiguration metricConfiguration,
			HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations,
			SaveDefinition saveDefinition,
			OptimizationAlgorithm optimizer
			) throws Exception {
		this.init(project_name, project_path, data, metricConfiguration, configurations, saveDefinition, optimizer);
	}
	
	public static Executor Executor(String project_name, String project_path) throws Exception {
		Executor ex = Executor.importSER(project_path+"Executor.ser");
		ex.db_con = DriverManager.getConnection("jdbc:sqlite:" + project_path + project_name + ".db");
		
		return ex;
	}
	
	private void init(
			String project_name,
			String project_path, 
			Data data, 
			MetricConfiguration metricConfiguration,
			HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations,
			SaveDefinition saveDefinition,
			OptimizationAlgorithm optimizer
			) throws Exception {
		this.projectName = project_name;
		this.project_path = project_path;
		File pp = new File(project_path);
		this.db_path = project_path + project_name + ".db";
		this.configurations = configurations;
		if(pp.exists() && new File(this.db_path).exists()) {
			throw new OperationNotSupportedException("\r\n"
					+ "In the given path a DB has been found. "
					+ "Please choose one of the following steps to resolve the error:\r\n"
					+ "          1) Change the given path.\r\n"
					+ "          2) Delete the DB in the given path.\r\n"
					+ "          3) Use the -static- method 'Executor' to load the project in the given path.\r\n");
		}
		pp.mkdirs();
        this.db_con = DriverManager.getConnection("jdbc:sqlite:" + this.db_path);
        this.metricConfiguration = metricConfiguration;
        this.saveDefinition = saveDefinition;
        
		this.registered_classes = new HashMap<String, Class<? extends Classifier>>();
		for(Entry<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> entry : configurations.entrySet()) {
			this.registered_classes.put(entry.getKey().getSimpleName(), entry.getKey());
		}
		// create table
		List<Integer> data_ids = Table_Management.createDB(
										new ArrayList<Class<? extends Classifier>>(registered_classes.values()),
										this.metricConfiguration.relevant_metric_methods, 
										data, 
										this.db_con,
										this.project_path
										);

		ResultSet ids = Table_Read.readDataSetIDs(this.db_con);
		Integer optimizerId = 1;
		while(ids.next()) {
			List<Pair<OptimizationAlgorithm,Boolean>> buffer = new ArrayList<Pair<OptimizationAlgorithm,Boolean>>();
			for(Entry<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> entry : configurations.entrySet()) {
				OptimizationAlgorithm oa = optimizer.clone();
				oa = SerializationUtils.clone(optimizer);
				oa.initAlgorithm(optimizerId, entry.getValue(), ids.getInt(1), entry.getKey(), saveDefinition, metricConfiguration, db_con, data_ids);
				Table_Updates.insert_optimizer(this.db_con, oa);
				buffer.add(
						new Pair<OptimizationAlgorithm, Boolean>(
								oa, 
								false)
						);
				
//				buffer.add(
//						new BayesianOptimization(entry.getValue(), ids.getInt(1), entry.getKey(), this.tbu, this.tr, saveDefinition, metricConfiguration, 10, 0.01, Metric.class.getMethod("calculateAccuracy", null), 20)
//						);
//				buffer.add(new BruteForce(entry.getValue(), ids.getInt(1), entry.getKey(), this.tbu, this.tr, this.tbm.data_ids, saveDefinition, metricConfiguration));
			optimizerId++;
			}
	//		this.searchAlgorithms.add(buffer);
		}
		this.exportSER(this.project_path+"Executor.ser");
	}
	
	
	public enum Pause_Type {
		Instant,
		Stop_before_next
	}
	public void abort_execution() {
		this.pause_execution(Pause_Type.Instant);
		File db_file = new File(this.db_path);
		db_file.delete();
	}

	public void pause_execution(Pause_Type pt) {
		if(pt.equals(Pause_Type.Instant)) {
			this.classifier_thread.shutdownNow();
		}else {
			this.classifier_thread.shutdown();
		}
		this.isActive = false;
	}
	
	public void restart_execution() throws Exception {
		Table_Updates.setAllUnfinished(this.db_con);
		Table_Management.clear_Result_Tables(this.db_con);

        this.start_execution();
	}
	
	public void start_execution() throws Exception {
		this.db_execution();
	}	

	private void db_execution() throws Exception {
		this.isActive = true;
		try {
			if(this.classifier_thread.isShutdown()) {
				this.classifier_thread = this.initExecutorService();
			}
			String data_name 		= "";
			ResultSet optimizers = Table_Read.readNextUnfinishedOptimizer(this.db_con);
			while(optimizers.next()) {
				OptimizationAlgorithm oa = (OptimizationAlgorithm) OptimizationAlgorithm.String2Object(optimizers.getString(2));
				Triple<Integer, Data_Set, Data_Set> loaded_data = null;
				System.out.println(oa.getClass());
				oa.setDBConn(this.db_con);
			    if(loaded_data == null || loaded_data.getFirst() != oa.dataID) {
			       	ResultSet data_result = Table_Read.readSameDataID(this.db_con, oa.dataID);
					while(data_result.next()) {
						loaded_data = new Triple<Integer, Data_Set, Data_Set>(
								oa.dataID, 
								Data_Set.import_SER(data_result.getString(2)), 
								Data_Set.import_SER(data_result.getString(3))
								);
						data_name = data_result.getString(4);
					}
			    }
			       
				Metric metric = null;
				Pair<Integer, HashMap<Integer, Object>> configuration = new Pair<Integer, HashMap<Integer,Object>>(null, null);
				while(oa.hasNext()) {
					configuration = oa.nextConfigurationString(metric, configuration.getValue());
					System.out.println("VALUE: " + oa.classType.getSimpleName() + configuration.getValue());
					if(configuration.getValue() != null) {
						Integer runID = configuration.getKey();
						
				        long startTime = System.currentTimeMillis();
				        Pair<Classifier, Metric> data = classifier_thread.submit(
						   							new Classifier_Executer(
						   										oa.classType,
						   										configuration.getValue(),
						   										loaded_data.getSecond(),
						   										loaded_data.getThird(),
						   										runID
						   									)
						   							).get();
				        long needed_time = System.currentTimeMillis() - startTime;
				        metric = data.getValue();
				        
				        Table_Updates.update_optimizer(this.db_con, oa);
				        this.exportSER(this.project_path+"Executor.ser");
				        // save Information to DB
				        Table_Updates.add_data(this.db_con, data, needed_time, this.metricConfiguration.relevant_metric_methods, runID);
				        // save Classifier to disk
				        this.saveDefinition.checkOptions(data, data_name, runID);
						// set Task to finished
				        Table_Updates.updateFinishedRun(this.db_con, runID);
				        this.iterationCount++;
					}else {
						break;
					}
					Table_Updates.update_optimizer(this.db_con, oa);
				}
				
			}
			this.classifier_thread.shutdown();
		}catch (Exception e) {
			// Stop the ExecutionService in case of error
			this.classifier_thread.shutdown();
			throw e;
		}
		this.isActive = false;
		this.exportSER(this.project_path+"Executor.ser");
	}

	private ExecutorService initExecutorService() {
		return Executors.newFixedThreadPool(1);
	}
	
	
	public static Executor importSER(String path) throws Exception{
        if (!path.toLowerCase().endsWith(".ser")) {
            throw new Data_Exception("The given path does not end with '.ser'!");
        }
        InputStream file = new FileInputStream(path);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        Executor exe = (Executor) input.readObject();
        input.close();
        exe.db_con = DriverManager.getConnection("jdbc:sqlite:" + exe.db_path);
        return exe;
    }

    public void exportSER(String path) throws Exception {
        if (!path.toLowerCase().endsWith(".ser")) {
            throw new Data_Exception("The given path does not end with '.ser'!");
        }
        File check_file = new File(path);
        check_file.getParentFile().mkdirs();
        check_file.createNewFile();
        OutputStream file = new FileOutputStream(path);
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);
        output.writeObject(this);
        output.close();
    }
    
    
    /*public Pair<Integer, Integer> getStatus() throws SQLException, ClassNotFoundException, IOException{
    	Integer wholeCount = 0;
    	Integer finCount = 0;
    	ResultSet optimizers = Table_Read.readAllOptimizer(this.db_con);
    	while(optimizers.next()) {
    		OptimizationAlgorithm oa = (OptimizationAlgorithm) OptimizationAlgorithm.String2Object(optimizers.getString(2));
    		if(!optimizers.getBoolean(3)){
				finCount += oa.getMaxCases();
			}
			wholeCount += oa.getMaxCases();
    	}
    	return new Pair<Integer, Integer>(finCount, wholeCount);
    }*/


	public Pair<String, Integer> getStatus() {
		String stringStatus = this.isActive ? "running" : "success";
		return new Pair<>(stringStatus, this.iterationCount);
	}

    
    public Connection getDBCon() {
    	return this.db_con;
    }
    public HashMap<String, Method> getMetrices() {
    	return this.metricConfiguration.relevant_metric_methods;
    }
	
    public HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> getConfiguration(){
    	return this.configurations;
    }
    public SaveDefinition getSaveDefinition() {
    	return this.saveDefinition;
    }
    public void removeDBCon() throws SQLException {
    	this.db_con.close();
    	this.db_con = null;
    }
}


