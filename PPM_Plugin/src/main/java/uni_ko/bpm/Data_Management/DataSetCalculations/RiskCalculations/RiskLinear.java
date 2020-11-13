package uni_ko.bpm.Data_Management.DataSetCalculations.RiskCalculations;

import java.util.List;

import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Flow;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;


public class RiskLinear extends DataSetCalculation{

	@Override
	public Flow calculate(Flow flow) {
		Data_Point failNode = flow.get_Fails_at();
		List<Data_Point> points = flow.get_flow();
		
		int failIndex = points.indexOf(failNode);
		if(failIndex == -1) {
			for(int index = failIndex; index > 0; index --) {
				points.get(index).putDataValue(this.getClass().getSimpleName(), Double.class, 0.0);
			}
		}else {
			double stepping = 100/(failIndex);
			for(int index = failIndex; index > 0; index --) {
				points.get(index).putDataValue(this.getClass().getSimpleName(), Double.class, (((double)index)*stepping));
			}
		}
		return new Flow(flow.get_process_Instance_Id(), points);
	}

}
