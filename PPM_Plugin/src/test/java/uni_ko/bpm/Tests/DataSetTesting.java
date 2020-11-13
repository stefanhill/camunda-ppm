package uni_ko.bpm.Tests;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Reader.Data_Reader;
import uni_ko.bpm.Data_Management.Data_Reader.History_Reader;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Util.DataSetUtils;
import uni_ko.bpm.cockpit.PPM_Plugin.CockpitPlugin;

import java.io.IOException;
import java.util.List;

public class DataSetTesting {

    public static void main(String[] args) throws Data_Exception {
        System.out.println(CockpitPlugin.isEclipse());
        /*String path = "PPM_Plugin\\src\\main\\resources\\testlog.xes";
        Data_Set ds = Data_Set.import_XES(path);
        System.out.println("done");
        Data_Set ds = Data_Set.import_XES("PPM_Plugin\\src\\test\\resources\\cs2k.xes");
        ds = DataSetUtils.randomizeDurations(ds, 10000);
        ds.export_XES("PPM_Plugin\\src\\test\\resources\\cs2k_d.xes");
        System.out.println(ds);*/
    }

    public void data_example() {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        Data_Reader dr = new History_Reader(processDefinitions.get(0).getId());

        Data_Set data_set;
        try {
            data_set = dr.get_instance_flows();
            // example of getting feature vectors
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println("Task-Name-List: " + data_set.<String>get_set_parameter("task_name"));
            System.out.println("Task-UID-List: " + data_set.<String>get_set_parameter("task_uid"));
            System.out.println("Task-Duration-List: " + data_set.<Long>get_set_parameter("duration"));
            System.out.println("Task-Duration-List: " + data_set.<Long>get_set_parameter("timestamp"));
            System.out.println("Task-Assignee-List: " + data_set.<Long>get_set_parameter("assignee"));

            // serialization example
            String path = System.getProperty("user.dir") + "\\test.ser";
            System.out.println("PATH" + path);
            data_set.export_SER(path);

            data_set = Data_Set.import_SER(path);
            System.out.println("__________________________________________________________________________");
            System.out.println("Task-Name-List: " + data_set.<String>get_set_parameter("task_name"));
            System.out.println("Task-UID-List: " + data_set.<String>get_set_parameter("task_uid"));
            System.out.println("Task-Duration-List: " + data_set.<String>get_set_parameter("duration"));
            System.out.println("Task-timestamp-List: " + data_set.<Long>get_set_parameter("timestamp"));
            System.out.println("Task-Assignee-List: " + data_set.<Long>get_set_parameter("assignee"));

            path = System.getProperty("user.dir") + "\\test.xes";
            System.out.println("PATH" + path);
            data_set.export_XES(path);

            data_set = Data_Set.import_XES(path, null);
            System.out.println("__________________________________________________________________________");
            System.out.println("Task-Name-List: " + data_set.<String>get_set_parameter("task_name"));
            System.out.println("Task-UID-List: " + data_set.<String>get_set_parameter("task_uid"));
            System.out.println("Task-Duration-List: " + data_set.<String>get_set_parameter("duration"));
            System.out.println("Task-timestamp-List: " + data_set.<Long>get_set_parameter("timestamp"));
            System.out.println("Task-Assignee-List: " + data_set.<Long>get_set_parameter("assignee"));

        } catch (Data_Exception e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Data_Set random_set = Data_Set.import_XES("C:\\Users\\nbart\\git\\camunda-ppm\\PPM_Plugin\\Dev_Data\\test01.xes", null);
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println("Task-Name-List: " + random_set.<String>get_set_parameter("task_name"));
            System.out.println("Task-UID-List: " + random_set.<String>get_set_parameter("task_uid"));
            System.out.println("Task-Duration-List: " + random_set.<String>get_set_parameter("duration"));
            System.out.println("Task-timestamp-List: " + random_set.<Long>get_set_parameter("timestamp"));
            System.out.println("Task-Assignee-List: " + random_set.<Long>get_set_parameter("assignee"));
        } catch (Data_Exception | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
