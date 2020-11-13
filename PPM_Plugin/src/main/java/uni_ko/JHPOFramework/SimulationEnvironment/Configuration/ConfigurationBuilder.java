package uni_ko.JHPOFramework.SimulationEnvironment.Configuration;

import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;

public class ConfigurationBuilder {
	
	private HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations = new HashMap<Class<? extends Classifier>, Pair<List<Integer>,List<Range>>>();
	private Class<? extends Classifier> classifier_type;
	private Configuration conf;
	
	public ConfigurationBuilder(Configuration conf) {
		this.conf = conf;
	}
	
	public ParameterBuilder add_classifier(Class<? extends Classifier> classifier_type) {
		this.classifier_type = classifier_type;
		return new ParameterBuilder(this);
	}
	public Configuration build() {
		this.conf.configurations = this.configurations;
		return this.conf;
	}

	protected void add_pair(List<Integer> ids, List<Range> ranges) {
		this.configurations.put(this.classifier_type, new Pair<List<Integer>, List<Range>>(ids, ranges));
	}
	
}
