package uni_ko.bpm.Data_Management.Data_Reader;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;

import java.util.List;

public class Running_Reader extends History_Reader {

    private String process_Instance_Id = null;

    public Running_Reader(String process_Definition_Id) {
        super(process_Definition_Id);
        this.instances = get_instances();
    }

    public Running_Reader(String process_Definition_Id, String process_Instance_Id) {
        super(process_Definition_Id);
        this.process_Instance_Id = process_Instance_Id;
        this.instances = this.get_instances_by_id(process_Instance_Id);
    }

    public int get_set_size() {
        return this.instances.size();
    }

    @Override
    protected List<HistoricProcessInstance> get_instances() {
        return this.history.createHistoricProcessInstanceQuery()
                .processDefinitionId(this.process_Definition_Id)
                .unfinished()
                .list();
        // also see History_Reader.get_instances()
    }

    protected List<HistoricProcessInstance> get_instances_by_id(String process_Instance_Id) {
        return this.history.createHistoricProcessInstanceQuery()
                .processInstanceId(process_Instance_Id)
                .list();
    }

    public Data_Set get_instance_flows(int number_of_flows) throws Data_Exception {
        if (number_of_flows > this.get_set_size()) {
            throw new Data_Exception("Requested number of paths is too long. Maximum size is " + this.get_set_size() + "!");
        }
        Data_Set set = new Data_Set(this.process_Definition_Id, null);
        while (number_of_flows > 0) {
            String process_Instance_ID = this.instances.get(this.current_number).getId();
            Flow flow = new Flow(process_Instance_ID);
            for (HistoricActivityInstance hai : this.get_activity_flow(process_Instance_ID)) {
                if (this.filter_activity_type(hai.getActivityType())) {
                    flow.add_Data_Point(hai, set.get_unique_Task_Name());
                }
            }
            set.add_Flow(flow);
            number_of_flows--;
            this.current_number++;
        }
        return set;
    }

    protected List<HistoricActivityInstance> get_activity_flow(String process_Instance_ID) {
        return this.history.createHistoricActivityInstanceQuery()
                .processInstanceId(process_Instance_ID)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
    }

}
