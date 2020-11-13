package uni_ko.JHPOFramework.SimulationEnvironment.Ranges;

import java.util.ArrayList;

public class Numeric_Range<T extends Number> extends Range<T> {
	public T from;
	public T to;
	public T stepping;
	
	// Cammunda plugin usage
	public Numeric_Range(Integer parameter_id, T from, T to, T stepping, String text, String info_text, Class<T> data_type) throws Exception {
		super(new ArrayList<T>(), parameter_id, text, info_text, data_type);
		this.from = from;
		this.to = to;
		this.stepping = stepping;

		this.calculateOptions();
	}
	
	// direct plugin useage
	public Numeric_Range(Integer parameter_id, T from, T to, T stepping, Class<T> data_type) throws Exception {
		super(new ArrayList<T>(), parameter_id, "", "", data_type);
		this.from = from;
		this.to = to;
		this.stepping = stepping;
		
		this.calculateOptions();
	}

	public void calculateOptions() throws Exception {
		if(Double.valueOf(stepping.doubleValue()) == 0.0 && !from.equals(to)) {
			throw new Exception("Stepping size will result in infinite range!");
		}
		this.options = new ArrayList<T>();
		this.options.add(from);
		T lFrom = this.from;
		T lTo = this.to;
		while(lFrom.doubleValue() < lTo.doubleValue()) {
			if(lFrom.getClass() == Integer.class) {
				lFrom = (T)Integer.valueOf(lFrom.intValue() + stepping.intValue());
			}else if(lFrom.getClass() == Double.class) {
				lFrom = (T)Double.valueOf(lFrom.doubleValue() + stepping.doubleValue());
			}
			this.options.add(lFrom);
		}
	}
}
