package uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Annotations.OptimizerOrdering;
import uni_ko.bpm.Annotations.OptimizerOrdering.OrderingOption;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Reflections.ReflectionReads;

public class SaveOption implements Serializable{
	protected double topPerrcent;
	public Integer topX = 0;
	protected String path;
	public transient Pair<String, Method> metric;
	protected OrderingOption best;
	protected boolean perClassifier;
	
	// <runID, <File, MetricValue> >
	public transient HashMap<Integer, Pair<File, Double>> savedClassifier;
	
	public SaveOption(double topPerrcent, String path, Pair<String, Method> metric, boolean perClassifier) throws UnsupportedEncodingException {
		super();
		this.topPerrcent = topPerrcent;
		this.path = path;
		this.metric = metric;
		
		this.best = metric.getSecond().getAnnotation(OptimizerOrdering.class).best();
		this.perClassifier = perClassifier;
		
		this.savedClassifier = this.read();
	}
	public SaveOption(Integer topX, String path, Pair<String, Method> metric, boolean perClassifier) throws UnsupportedEncodingException {
		super();
		this.topX = topX;
		this.path = path;
		this.metric = metric;
		this.best = metric.getSecond().getAnnotation(OptimizerOrdering.class).best();
		this.perClassifier = perClassifier;
		
		this.savedClassifier = this.read();
	}

	public void checkForSave(Pair<Classifier, Metric> result, HashMap<String, Integer> classifierDistribution, int wholeCount, String data_name, int runID) throws Exception {
		Double metricValue = (Double) metric.getValue().invoke(result.getValue(), null);
		metricValue = (metricValue == null) ? 0.0 : metricValue;
		boolean toSave = false;
		String simpleName = result.getKey().getClass().getSimpleName();
		if(this.perClassifier) {
			toSave = this.checkForSave(classifierDistribution.get(simpleName), metricValue, simpleName);
		}else {
			toSave = this.checkForSave(wholeCount, metricValue, simpleName);
		}
		if(toSave) {
			File f = this.save(metricValue, result.getKey(), data_name);
			this.savedClassifier.put(runID, new Pair<File, Double>(f, metricValue));
		}
	}
	private boolean checkForSave(int countApplicable, Double metricValue, String cType) throws Exception {
		int numberToSave = (this.topX == 0) ? (int) Math.ceil(countApplicable * this.topPerrcent) : this.topX;
		if(this.getSavedSize(cType) < numberToSave) {
			return true;
		}else {
			Entry<Integer, Pair<File, Double>> worst = this.getWorst(cType);
			if(worst != null && this.compare(metricValue, worst.getValue().getValue())) {
				this.savedClassifier.remove(worst.getKey());
				Classifier worstClassifier = ReflectionReads.deserializeClassifier(worst.getValue().getKey(), (worst.getValue().getKey().getName().split("\\$"))[0]);
				if(worstClassifier != null) {
					for(File wf : worstClassifier.get_subfiles()) {
						wf.delete();
					}
				}
				worst.getValue().getKey().delete();
				return true;
			}else {
				return false;
			}
		}
	}
	private boolean compare(Double newValue, Double oldValue) {
		if(this.best.equals(OptimizerOrdering.OrderingOption.High)) {
			return newValue > oldValue;
		}else {
			return newValue < oldValue;
		}
	}
	
	private HashMap<Integer, Pair<File,Double>> read() throws UnsupportedEncodingException {
		File p = new File(this.path);
		if(!p.exists()) {
			p.mkdirs();
			return new HashMap<Integer, Pair<File,Double>>();
		}else {
			HashMap<Integer, Pair<File,Double>> cBuffer = new HashMap<Integer, Pair<File,Double>>();
			for(File f : p.listFiles()) {
				String fileName = f.getName();
				if(fileName.endsWith(".ser")) {
					fileName = fileName.substring(0, fileName.length() - 4);
					String decoded = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
					String[] parts = decoded.split(File.separator+"$");
					double metricValue = Double.valueOf(parts[1]);
					
					cBuffer.put(Integer.valueOf(parts[0]), new Pair<File, Double>(f, metricValue));
				}
			}
			return cBuffer;
		}
	}
	private File save(Double metricValue, Classifier classifier, String data_name) throws Exception {
		String fp =	this.path +
				data_name +
				File.separator +
				URLEncoder.encode(classifier.getClass().getSimpleName(), StandardCharsets.UTF_8.toString()) +
				"$" + 
				URLEncoder.encode(metricValue.toString(), StandardCharsets.UTF_8.toString()) +
				"$" +
				URLEncoder.encode(classifier.get_given_name(), StandardCharsets.UTF_8.toString()) +
				".ser";
		classifier.exportSER(fp);
		return new File(fp);
	}

	
	
	private Entry<Integer, Pair<File, Double>> getWorst(String cType) {
		Comparator<Map.Entry<Integer, Pair<File, Double>>> comp = (Map.Entry<Integer, Pair<File, Double>> e1, Map.Entry<Integer, Pair<File, Double>> e2)
				->e1.getValue().getValue().compareTo(e2.getValue().getValue());
		if(this.perClassifier) {
			if(this.best.equals(OptimizerOrdering.OrderingOption.High)) {
				return this.savedClassifier.entrySet().stream()
						.filter(e -> (e.getValue().getKey().getName().split("\\$"))[0].equals(cType))
						.min(comp)
						.orElse(null);
			}else {
				return this.savedClassifier.entrySet().stream()
						.filter(e -> (e.getValue().getKey().getName().split("\\$"))[0].equals(cType))
						.max(comp)
						.orElse(null);
			}
		}else {
			if(this.best.equals(OptimizerOrdering.OrderingOption.High)) {
				return this.savedClassifier.entrySet().stream()
						.min(comp)
						.get();
			}else {
				return this.savedClassifier.entrySet().stream()
						.max(comp)
						.get();
			}
		}
	}
	private Integer getSavedSize(String cType) {
		if(this.perClassifier) {
			return (int) this.savedClassifier.entrySet().stream()
					.filter(e -> (e.getValue().getKey().getName().split("\\$"))[0].equals(cType))
					.count();
		} else {
			return this.savedClassifier.size();
		}
	}
	
	
	
	protected Pair<String, String> methodStr;
	protected List<Triple<Integer, String, Double>> savedStr;
    private void writeObject(ObjectOutputStream out) throws IOException, SecurityException
    {
    	this.methodStr = new Pair<String, String>(metric.getKey(), metric.getValue().getName());
    	this.savedStr = new ArrayList<Triple<Integer,String,Double>>();
    	for(Entry<Integer, Pair<File, Double>> entry : this.savedClassifier.entrySet()) {
    		this.savedStr.add(new Triple<Integer, String, Double>(entry.getKey(), entry.getValue().getFirst().getAbsolutePath(), entry.getValue().getValue()));
    	}
    	out.defaultWriteObject();
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException
    {
    	in.defaultReadObject();
    	this.metric = new Pair<String, Method>(methodStr.getKey(), Metric.class.getMethod(methodStr.getValue(), null));
    	this.methodStr = null;
    	this.savedClassifier = new HashMap<Integer, Pair<File,Double>>();
    	for(Triple<Integer, String, Double> trip : this.savedStr) {
    		this.savedClassifier.put(trip.getLeft(), new Pair<File, Double>(new File(trip.getMiddle()), trip.getRight()));
    	}
        read();
    }
	
	
	
	
	
	
	
	
	
	
	
	 

}
