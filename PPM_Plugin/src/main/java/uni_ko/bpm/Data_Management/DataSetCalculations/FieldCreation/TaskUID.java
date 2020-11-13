package uni_ko.bpm.Data_Management.DataSetCalculations.FieldCreation;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;

public class TaskUID extends DataSetCalculation{
	
	private Data_Set ds;
	public TaskUID(Data_Set ds) {
		this.ds = ds;
	}
	@Override
	public Flow calculate(Flow flow) {
		for(Data_Point dp : flow.flow){
			if(dp.getDataValues().get("concept:name").equals("Unknown")) {
				System.out.println("WUUT");
			}
			if(dp.getDataValues().get("concept:name") == null) {
				dp.putDataValue("task_uid", Integer.class, null);
			}else {
				dp.putDataValue("task_uid", Integer.class, ds.get_unique_Task_Name().get(dp.getDataValues().get("concept:name").getValue().toString()));
			}
			
		}
		return flow;
	}

}
