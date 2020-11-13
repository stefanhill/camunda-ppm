package uni_ko.bpm.Machine_Learning.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Numeric_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;

public class Parameter_Communication_Wrapper<T> {
	public Class<T> data_type;
	
    public Integer parameter_id;
    public String text;
    public String info_text;

    public T current_value;

    public boolean is_Extended_Configuration;
    
    public Parameter_Communication_Wrapper(Integer parameter_id, String text, String info_text, T current_value, Class<T> data_type, boolean is_Esthetic) {
        super();
        this.parameter_id = parameter_id;
        this.text = text;
        this.info_text = info_text;
        this.current_value = current_value;
        this.data_type = data_type;
        this.is_Extended_Configuration = is_Esthetic;
    }

    public String toString() {
        return parameter_id.toString() + "," + current_value;
    }

	public Range toRange() throws Exception { return null;}
	public Range toRange(Double lower, Double upper) throws Exception { return null;}
}
