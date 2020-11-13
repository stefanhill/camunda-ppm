package uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.DB_Utils.Table_Read;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;

public class SaveDefinition implements Serializable{
	private HashMap<String, List<SaveOption>> optionsPerDataSet = new HashMap<String, List<SaveOption>>();
	private List<SaveOption> preOptions = new ArrayList<SaveOption>();

	// <ClassifierSimpleName, Count>
	private HashMap<String, Integer> classifierDistribution = new HashMap<String, Integer>();
	private int wholeCount = 0;

	private boolean isSet = false;
	public SaveDefinitionBuilder builder() {
		return new SaveDefinitionBuilder(this);
	}
	
	
	
//	public void setClassifierDistribution(
//				Connection db_con, 
//				Table_Read tr, 
//				HashMap<String, Class<? extends Classifier>> registered_classes
//			) throws SQLException {
//		
//		ResultSet classifierCounts = tr.readClassifierCounts(db_con);
//		while(classifierCounts.next()) {
//			String simpleName 	= classifierCounts.getString(1);
//			int count 			= classifierCounts.getInt(2);
//			this.classifierDistribution.put(simpleName, count);
//			this.wholeCount += count;
//		}
//
//		ResultSet dataSets = tr.readDataSetNames(db_con);
//		while(dataSets.next()) {
//			optionsPerDataSet.put(dataSets.getString(1), new ArrayList<SaveOption>(this.preOptions));
//		}
//		this.isSet = true;
//	}
	public void setClassifierDistribution(Connection db_con, int maxClassifier, Class<? extends Classifier> type) throws SQLException {
		this.classifierDistribution.put(type.getSimpleName(), maxClassifier);
		this.wholeCount += maxClassifier;
		
		ResultSet dataSets = Table_Read.readDataSetNames(db_con);
		while(dataSets.next()) {
			optionsPerDataSet.put(dataSets.getString(1), new ArrayList<SaveOption>(this.preOptions));
		}
	}
	
	public void validate(HashMap<String, Method> relevant_metric_methods) {
		for(SaveOption option : this.preOptions) {
			if(relevant_metric_methods.containsKey(option.metric.getKey())) {
				if(relevant_metric_methods.get(option.metric.getKey()).hashCode() != option.metric.getValue().hashCode()) {
					throw new UnsupportedOperationException("It is not possible to save Classifier based on Metrices which are unknown to the Executer");
				}
			}else {
				throw new UnsupportedOperationException("It is not possible to save Classifier based on Metrices which are unknown to the Executer");
			}
		}
		if(this.isSet) {
			this.preOptions = null;
		}
	}
	public boolean isSet() {
		return this.isSet;
	}
	protected void addOption(SaveOption so) {
		this.preOptions.add(so);
	}
	public void checkOptions(Pair<Classifier, Metric> result, String data_name, int runID) throws Exception {
		for(SaveOption option : this.optionsPerDataSet.get(data_name)) {
			option.checkForSave(result, this.classifierDistribution, this.wholeCount, data_name, runID);
		}
	}

	public List<SaveOption> getSaveOptions(){
		return this.preOptions;
	}
}
