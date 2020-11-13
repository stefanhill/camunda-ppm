package uni_ko.bpm.Machine_Learning;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;

public abstract class Classifier implements Serializable, Cloneable {
	
	protected transient ClassifierMeta meta_data 	= null;
	
	public Metric last_calculated_metric 			= null;
	protected List<File> sub_files					= new ArrayList<>();
	
	public boolean training_allowed					= true;
	
	protected List<PredictionType> prediction_types;
    /**
     * Performs training on a training set an tests results with the testing set
     *
     * @param training_set Data set for training
     * @param test_set     Data set for testing
     * @return Metric with the training results
     * @throws Exception
     */
    public abstract Metric train(Data_Set training_set, Data_Set test_set) throws Exception;

    public Metric get_metric() throws Exception{
    	if(this.last_calculated_metric == null) {
    		throw new ClassificationException("No previous Metric was calculated!");
    	}
    	return this.last_calculated_metric;
    }
    /**
     * Splits a data set into a training and a testing set with the ratio 0.8/0.2 and performs training
     *
     * @param training_set Data set for testing and training
     * @return Metric with the training results
     * @throws Exception
     */
    public Metric train(Data_Set training_set) throws Exception {
        try {
            List<Data_Set> sets = training_set.split(0.8, 0.2);
            return this.train(sets.get(0), sets.get(1));
        } catch (Data_Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void stop_training() throws Exception {
    	this.training_allowed = false;
    }
    
    public abstract List<Parameter_Communication_Wrapper> configurational_parameters();
    public abstract void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception;
    
    /**
     * Classifies a list of instances and returns a list of classification results
     *
     * @param data_set
     * @return a list of classification results
     * @throws Exception
     */
    public abstract List<Classification> evaluate(Data_Set data_set) throws Exception;

    
    public String get_Classifier_Name() {
    	return this.getClass().getSimpleName();
    }

    public String get_given_name() {
    	return this.meta_data.get_given_name();
    }
    public int get_version() {
    	return this.meta_data.getVersion();
    }
    public void change_version(int version) {
    	this.meta_data.setVersion(version);
    }
    public Date get_creationTime() {
    	return this.meta_data.getCreationTime();
    }
    public Date get_lastModified() {
    	return this.meta_data.getLastModified();
    }
    public void change_lastModified(Date last_Modified) {
    	this.meta_data.setLastModified(last_Modified);
    }
    public ClassifierMeta get_Meta_Data() {
    	return this.meta_data;
    }
    public void set_Meta_Data(ClassifierMeta classifier_meta) {
    	this.meta_data = classifier_meta;
    }
    
    
    public abstract List<PredictionType> get_prediction_type();
	
    /**
     * Imports a Classifier from local file
     *
     * @param path Path to local import file
     * @return 
     * @return Lookup the implementation for details
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public <T> T importSER(String path) throws Exception{
        if (!path.toLowerCase().endsWith(".ser")) {
            throw new Data_Exception("The given path does not end with '.ser'!");
        }
        InputStream file = new FileInputStream(path);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        T recovered = (T) input.readObject();
        input.close();

        return recovered;
    }

    /**
     * Exports a Classifier to a local file
     *
     * @param path Path to local export file
     * @throws IllegalArgumentException
     * @throws IOException
     */
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

    public Classifier clone() throws CloneNotSupportedException {
        return (Classifier) super.clone();
    }
    public boolean is_creatable() {
    	return true;
    }
    public boolean is_trainable() {
    	return true;
    }
    
    public void add_subfile(File file) {
    	if(this.sub_files == null) {
    		this.sub_files = new ArrayList<File>();
    	}
    	this.sub_files.add(file);
    }
    public List<File> get_subfiles() {
    	return this.sub_files;
    }
 
}
