package uni_ko.bpm.Machine_Learning.Util;

import java.util.Map;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Numeric_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;

public class Parameter_Communication_Wrapper_Single<T extends Number> extends Parameter_Communication_Wrapper {
	
	public T suggested_value;
	
	public Parameter_Communication_Wrapper_Single(
			Integer parameter_id, 
			String text, 
			String info_text,
			boolean is_Extended_Configuration,
			T current_value,
			Class<T> data_type, 
			T suggested_value) {
		super(parameter_id, text, info_text, current_value, data_type, is_Extended_Configuration);
		this.data_type = data_type;
		this.suggested_value = suggested_value;
	}

	public String toString() {
		return super.toString();
	}

	public Range toRange() throws Exception {
		double upperMul = (this.is_Extended_Configuration) ? 1.0 : 1.5;
		double lowerMul = (this.is_Extended_Configuration) ? 1.0 : 0.5;
		return this.toRange(lowerMul, upperMul);
	}

	public Range toRange(Double lowerMul, Double upperMul) throws Exception {
		Numeric_Range range = null;
		double stepping = (this.is_Extended_Configuration) ? 0.0 : Double.MAX_VALUE;
		upperMul = (this.is_Extended_Configuration) ? 1.0 : upperMul;
		lowerMul = (this.is_Extended_Configuration) ? 1.0 : lowerMul;
	    if (data_type.equals(Integer.class)) {
	    	int lower = (int)((Integer.parseInt(this.suggested_value.toString()))*lowerMul);
	    	int upper = (int)((Integer.parseInt(this.suggested_value.toString()))*upperMul);
	    	range = new Numeric_Range<Integer>(
	    				this.parameter_id,
	    				lower,
	    				upper,
	    				(int) Math.min( Math.max(1, ((upper - lower) / 10)), stepping),
	    				this.text,
	    				this.info_text,
	    				this.data_type
	    				);
	    }else if (data_type.equals(Long.class)) {
	    	long lower = (long)((Long.parseLong(this.suggested_value.toString()))*lowerMul);
	    	long upper = (long)((Long.parseLong(this.suggested_value.toString()))*upperMul);
	    	range = new Numeric_Range<Long>(
	    				this.parameter_id,
	    				lower,
	    				upper,
	    				(long) Math.min( Math.max(1, ((upper - lower) / 10)), stepping),
	    				this.text,
	    				this.info_text,
	    				this.data_type);
	    }else {
	    	range = new Numeric_Range<Double>(
	    				this.parameter_id,
					Double.parseDouble(this.suggested_value.toString())*lowerMul,
					Double.parseDouble(this.suggested_value.toString())*upperMul,
	    				(((Double.parseDouble(this.suggested_value.toString())*upperMul) - (Double.parseDouble(this.suggested_value.toString())*lowerMul)) / 10),
	    				this.text,
	    				this.info_text,
	    				this.data_type);
	    }
	    range.calculateOptions();
	    return range;
	}
}
