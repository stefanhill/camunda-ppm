package uni_ko.JHPOFramework.SimulationEnvironment.Ranges;

import java.io.Serializable;
import java.util.List;

public class Range<T> implements Serializable{
	public Class<?> data_type;
	
	public Integer parameter_id;
    public String text;
    public String info_text;
    
	public List<T> options;

	public Range(List<T> options, Integer parameter_id, String text, String info_text, Class<?> data_type) throws InstantiationException, IllegalAccessException {
		this.options = options;
		this.parameter_id = parameter_id;

		this.text = text;
		this.info_text = info_text;
		this.data_type = data_type;
	}
	public void calculateOptions() throws Exception {}
	public Range(){}
}
