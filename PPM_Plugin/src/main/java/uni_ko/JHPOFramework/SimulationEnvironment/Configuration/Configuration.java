package uni_ko.JHPOFramework.SimulationEnvironment.Configuration;

import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;

public class Configuration {
	protected HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations;
	
	public Configuration() {}
	public Configuration(HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations) {
		this.configurations = configurations;
	}
	
	
	public ConfigurationBuilder builder() {
		return new ConfigurationBuilder(this);
	}
	
	public HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> get_conf_raw(){
		return this.configurations;
	}
}
