package uni_ko.bpm.Machine_Learning.WekaClassifier;

import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Reader.Token_Reader;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;

import java.util.HashMap;

public class WekaUtilitiesTesting {

    /*public static void main(String[] args) throws IllegalAccessException, NoSuchFieldException, Data_Exception {
        WekaUtilitiesTesting.testDataInstances();
    }

    public static void testDataInstances() throws IllegalAccessException, NoSuchFieldException, Data_Exception {
        String pid = "PID";
        Data_Set ds = new Data_Set(pid);
        HashMap<String, Integer> utn = new HashMap<>();
        ds.set_unique_Tokens(utn);

        for (int i = 0; i < 20; i++) {
            utn.put("Task" + i, i);
        }

        Flow f1 = new Flow(pid);

        for (int i = 0; i < 20; i++) {
            Data_Point dp = new Data_Point();
            dp.set_task_name(utn, "Task" + i);
            dp.set_duration(100L);
            dp.setQuadratic_risk(0.0);
            f1.add_Data_Point(dp);
        }

        ds.add_Flow(f1);

        Flow f2 = new Flow(pid);

        for (int i = 0; i < 5; i++) {
            Data_Point dp = new Data_Point();
            dp.set_task_name(utn, "Task" + i);
            dp.set_duration(100L);
            dp.setQuadratic_risk(0.0);
            f2.add_Data_Point(dp);
        }

        ds.add_Flow(f2);

        Token_Reader reader = new Token_Reader(ds, 7, 0);
        Data_Set tokenSet = reader.get_instance_flows();

    }*/
}
