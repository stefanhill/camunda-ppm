package uni_ko.bpm.Data_Management.Util;

import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Deprecated
public class DataSetUtils {

    private DataSetUtils() {}

    public static Data_Set randomizeDurations(Data_Set ds, int maxDuration) {
        Map<String, Integer> unique_task_names = ds.get_unique_Task_Name();
        Map<String, Long> unique_time_expectations = new HashMap<>();
        for (String unique_task_id :
                unique_task_names.keySet()) {
            unique_time_expectations.put(unique_task_id, (long) (Math.random() * maxDuration));
        }
        for (Flow flow :
                ds.data_set) {
            for (Data_Point dataPoint :
                    flow.get_flow()) {
                // TODO: 18.09.2020 Use new DataPoint Notation 
                /*if (dataPoint.getTask_name() != null) {
                    dataPoint.set_duration((long) ((new Random().nextGaussian() + 1) * unique_time_expectations.get(dataPoint.getTask_name())));
                }*/
            }
        }
        return ds;
    }
}
