package uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.management.InvalidAttributeValueException;

import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.CheckMethods;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Metric;


public class SaveDefinitionBuilder {
	private SaveDefinition sd;
	
	public SaveDefinitionBuilder(SaveDefinition sd) {
		this.sd = sd;
	}
	
	public SaveDefinitionBuilder saveTopPercent(double topPerrcent, String path, Pair<String, Method> onMetric) throws UnsupportedEncodingException, InvalidAttributeValueException{
		CheckMethods.checkMethod(onMetric.getValue());
		path = path + "Top"+topPerrcent*100+"Percent_"+onMetric.getKey()+File.separator;
		sd.addOption(new SaveOption(topPerrcent, path, onMetric, false));
		return this;
	}
	public SaveDefinitionBuilder saveTopPercentPerClassifer(double topPerrcent, String path, Pair<String, Method> onMetric) throws UnsupportedEncodingException, InvalidAttributeValueException {
		CheckMethods.checkMethod(onMetric.getValue());
		path = path + "Top"+topPerrcent*100+"PercentPerClassifier_"+onMetric.getKey()+File.separator;
		sd.addOption(new SaveOption(topPerrcent, path, onMetric, true));
		return this;
	}
	public SaveDefinitionBuilder saveTopXClassifer(Integer topX, String path, Pair<String, Method> onMetric) throws UnsupportedEncodingException, InvalidAttributeValueException {
		CheckMethods.checkMethod(onMetric.getValue());
		path = path + "Top"+topX+"Classifier_"+onMetric.getKey()+File.separator;
		sd.addOption(new SaveOption(topX, path, onMetric, true));
		return this;
	}
	
	public SaveDefinition build() {
		return this.sd;
	}
	
	
	
	
	
}
