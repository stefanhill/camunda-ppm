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

import java.util.Date;
import java.util.List;

import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;

public class ClassifierMeta implements Serializable{
	
	private static final long serialVersionUID = -6625446362827347367L;
	private Classifier_Short_Description short_description;
	private List<PredictionType> predictionTypes;

	private Date creationTime;
	private Date lastModified;
	private String author;

	private boolean is_creatable;
	private boolean is_trainable;
	
	private Metric previous_metric;

	private double acurracy;
	
	public ClassifierMeta() {}
	public ClassifierMeta(Classifier_Short_Description short_description) {
		this.short_description = short_description;
	}
	public ClassifierMeta(String given_name, Integer version, List<PredictionType> predictionTypes,
			Date creationTime, Date lastModified, String author,
			Boolean is_publicly_available,
			Classifier c) {
		super();
		this.short_description = new Classifier_Short_Description(given_name, version, is_publicly_available);
		this.predictionTypes = predictionTypes;
		this.creationTime = creationTime;
		this.lastModified = lastModified;
		this.author = author;
		
		this.is_creatable = c.is_creatable();
		this.is_trainable = c.is_trainable();
		
	}
	
	public Metric get_Metric() {
		return this.previous_metric;
	}
	public void set_metric(Metric metric) {
		this.previous_metric = metric;
	}

	public String get_given_name() {
		return this.short_description.given_name;
	}

	public void set_given_name(String given_name) {
		this.short_description.given_name = given_name;
	}

	public Integer getVersion() {
		return this.short_description.version;
	}

	public void setVersion(Integer version) {
		this.short_description.version = version;
	}

	public List<PredictionType> getPredictionTypes() {
		return predictionTypes;
	}

	public void setPredictionTypes(List<PredictionType> predictionTypes) {
		this.predictionTypes = predictionTypes;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	public Boolean getIs_publicly_available() {
		return this.short_description.is_publicly_available;
	}
	public void setIs_publicly_available(Boolean is_publicly_available) {
		this.short_description.is_publicly_available = is_publicly_available;
	}
	
    public boolean isIs_creatable() {
		return is_creatable;
	}
	public void setIs_creatable(boolean is_creatable) {
		this.is_creatable = is_creatable;
	}
	public boolean isIs_trainable() {
		return is_trainable;
	}
	public void setIs_trainable(boolean is_trainable) {
		this.is_trainable = is_trainable;
	}

	public double getAcurracy() { return acurracy; }

	public void setAcurracy(double acurracy) { this.acurracy = acurracy; }
	
	public void updateAcurracy() throws Metric_Exception {
		if(this.previous_metric == null){
			this.acurracy = 0d;
			return;
		}
		double acurracy = this.previous_metric.accuracy();
		this.acurracy = acurracy;
	}
	
	public Classifier_Short_Description getShort_description() {
		return short_description;
	}
	
	
	public static ClassifierMeta import_SER(String path) throws Data_Exception, IOException, ClassNotFoundException {
        if (!path.toLowerCase().endsWith(".ser")) {
            throw new Data_Exception("The given path does not end with '.ser'!");
        }
        InputStream file = new FileInputStream(path);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        ClassifierMeta recovered = (ClassifierMeta) input.readObject();
        input.close();
        buffer.close();
        file.close();

        return recovered;
    }
    public void export_SER(String path) throws Data_Exception, IOException {
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
        buffer.close();
        file.close();
    }
	

}
