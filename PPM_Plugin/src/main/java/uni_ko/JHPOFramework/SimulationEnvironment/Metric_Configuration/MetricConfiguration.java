package uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration;

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
import java.util.HashMap;
import java.util.Map.Entry;

import javax.management.InvalidAttributeValueException;

import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Machine_Learning.Metric;

public class MetricConfiguration implements Serializable{
	
	public transient HashMap<String, Method> relevant_metric_methods = new HashMap<String, Method>();
	public MetricConfiguration() {}
	
	public MetricConfiguration addMetric(String name, Method method) throws InvalidAttributeValueException {
		CheckMethods.checkMethod(method);
		if(this.relevant_metric_methods.containsKey(name)) {
			throw new InvalidAttributeValueException("The name " + name + " has been given twice. This would result in DB issues. Please consider to rename one of the occurences");
		}
		relevant_metric_methods.put(name, method);
		return this;
	}
	
	
	


	protected HashMap<String, String> rmm_ser;
    private void writeObject(ObjectOutputStream out) throws IOException, SecurityException
    {
        this.rmm_ser = new HashMap<String,String>();
        for(Entry<String, Method> entry : this.relevant_metric_methods.entrySet()) {
        	this.rmm_ser.put(
        			entry.getKey(), 
        			entry.getValue().getName()
        			);
        }
    	out.defaultWriteObject();
       
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException
    {
    	in.defaultReadObject();
        this.relevant_metric_methods = new HashMap<String, Method>();
        for(Entry<String, String> entry : this.rmm_ser.entrySet()) {
        	this.relevant_metric_methods.put(entry.getKey(), Metric.class.getMethod(entry.getValue(), null));
        }
        this.rmm_ser = null;
    }
    
    
    
}
