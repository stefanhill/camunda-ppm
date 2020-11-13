package uni_ko.bpm.Data_Management.DataSetCalculations.NaN;

import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;
import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Flow;

public class ReplaceNaN extends DataSetCalculation{

	private final String fieldName;
	private final transient Object value;
	private final transient Class<?> className;

	public ReplaceNaN(String fieldName, Class<?> className, Object value) {
		this.fieldName = fieldName;
		this.value = value;
		this.className = className;
	}
	@Override
	public Flow calculate(Flow flow) {
		for(Data_Point dp : flow.flow){
			if(dp.getDataValues().get(this.fieldName) == null) {
				dp.putDataValue(this.fieldName, this.className, this.value);
			}
		}
		return flow;
	}

}
