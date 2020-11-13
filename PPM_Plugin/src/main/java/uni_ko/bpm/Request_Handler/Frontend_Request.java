package uni_ko.bpm.Request_Handler;

import org.apache.commons.math3.util.Pair;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public interface Frontend_Request{
	// classifier_name   -  Name of the CLassifier eg. LSTM_Classifier  or   NgramCLassifier
	// given_name 		 -  The special name of a CLassifier eg.    a instance of the classifier LSTM_Classifier has the given Name "James_The_Third"
	
	public void set_PDI(String processDefinitionId) throws Exception;
	
	// get all possible classifier types
	public List<String> get_possible_classifier() throws Exception;
	
	//
	public List<PredictionType> get_prediction_type(String name, boolean is_given_name)throws Exception;
	
	// <Classifier_Short_Description, is_default>
	public List<Pair<Classifier_Short_Description, Boolean>> get_classifier(Boolean get_trainable, Boolean already_trained, Boolean is_public) throws Exception;
	
	// meta handling
	public ClassifierMeta get_meta_data(String given_name) throws Exception;
	public void set_meta_data(String given_name, ClassifierMeta meta_data) throws Exception;
	
	// creation handling
	public List<Parameter_Communication_Wrapper> get_classifier_parameter_information(String classifier_name) throws Exception;
	public List<Parameter_Communication_Wrapper> get_classifier_current_parameter_information(String given_name) throws Exception;
	
	public void create_classifier(String given_name, String classifier_name, HashMap<Integer, Object> parameters, String author) throws Exception;
	
	public void copy_classifier(String given_name, String new_name) throws Exception;
	public void rename_classifier(String given_name, String new_name) throws Exception;
	
	public List<Classification> classify_Instance(String given_name, String process_Instance_Id) throws Exception;
	
	public Metric get_metric(String given_name) throws Exception;
	// Admin functions
	public void set_new_default(String given_name) throws Exception;
	public void set_public(String given_name, boolean is_publicly_available) throws Exception;
	
	public void delete_classifier(String given_name) throws Exception;
	public void delete_classifier_version(String given_name, String version) throws Exception;
	
	public void revert_classifier(String given_name) throws Exception;
	public void revert_classifier(String given_name, Integer to_version) throws Exception;
	
	public Metric train(String given_name) throws Exception;
	
	public Metric train(String given_name, InputStream data_stream) throws Exception;
	public Metric train(String given_name, InputStream training_stream, InputStream evaluation_stream) throws Exception;
	public Metric train(String given_name, InputStream data_stream, double training_ratio, double evaluation_ratio) throws Exception;

	public Metric train(String given_name, double ratio) throws Exception;

	public Metric train(String given_name, Date start_date, Date finished_date) throws Exception;
	public Metric train(String given_name, double ratio, Date start_date, Date finished_date) throws Exception;

    public Metric train(String given_name, Data_Set data_set, double training_ratio, double evaluation_ratio) throws Exception;
	public Metric train(String given_name, String data_set_path, double training_ratio, double evaluation_ratio) throws Exception;
	public Metric train(String given_name, String training_set_path, String evaluation_set_path) throws Exception;
	public Metric train(String given_name, Data_Set training_set, Data_Set evaluation_set) throws Exception;

	public void add_Classifier(Classifier classifier) throws Exception;
	
	public void set_training_status(String given_name, boolean success, boolean done, double acurracy) throws Exception;
	public TrainingStatus get_training_status(String given_name) throws Exception;
	
	public Future<Boolean> asyc_Request (Runnable request);
	
	public void stop_training(String given_name) throws Exception;

	public void merge_Classifier(String new_given_name, String author, String... given_names) throws Exception;
	public void merge_Classifier(String new_given_name, String author, List<String> given_names) throws Exception;
}
