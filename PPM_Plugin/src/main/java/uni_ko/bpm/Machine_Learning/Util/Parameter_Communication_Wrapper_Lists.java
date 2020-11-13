package uni_ko.bpm.Machine_Learning.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dataclass.data;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.List_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Numeric_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;

public class Parameter_Communication_Wrapper_Lists<T> extends Parameter_Communication_Wrapper{
	
	
	public List<T> possible_value;
	
	public Boolean allow_multi_select;

	public Parameter_Communication_Wrapper_Lists(
			Integer parameter_id, 
			String text, 
			String info_text,
			boolean is_Extended_Configuration,
			List<T> current_value, 
			Class<T> data_type, 
			List<T> possible_value, 
			Boolean allow_multi_select) {
		super(parameter_id, text, info_text, current_value, data_type, is_Extended_Configuration);
		this.data_type = data_type;
		this.possible_value = possible_value;
		this.allow_multi_select = allow_multi_select;
	}

	public String toString() {
		return super.toString() + ","+allow_multi_select;
	}
	
	public Range toRange() throws Exception {
		List_Range range = new List_Range<T>(this.parameter_id,
				new ArrayList(Arrays.asList(this.possible_value)),
				this.text,
				this.info_text,
				this.data_type,
				this.allow_multi_select);
		range.calculateOptions();
		return range;
	
	}

	public Range toRange(Double lower, Double upper) throws Exception {
		return this.toRange();
	}
}
