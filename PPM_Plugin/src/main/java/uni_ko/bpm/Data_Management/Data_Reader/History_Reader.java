package uni_ko.bpm.Data_Management.Data_Reader;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;

import java.util.Date;
import java.util.List;

public class History_Reader extends Data_Reader {

    protected ProcessEngine processEngine;
    protected HistoryService history;

    protected List<HistoricProcessInstance> instances;
    protected int current_number = 0;

    protected Date start_date;
    protected Date finished_date;

    public History_Reader(String process_Definition_Id) {
        super(process_Definition_Id);
        this.processEngine = ProcessEngines.getDefaultProcessEngine();
        this.history = processEngine.getHistoryService();

        this.start_date = new Date(Long.MIN_VALUE);
        this.finished_date = new Date(Long.MAX_VALUE);

        this.instances = this.get_instances();
    }

    public History_Reader(String process_Definition_Id, Date start_date, Date finished_date) {
        super(process_Definition_Id);
        this.processEngine = ProcessEngines.getDefaultProcessEngine();
        this.history = processEngine.getHistoryService();

        this.start_date = start_date;
        this.finished_date = finished_date;

        this.instances = this.get_instances();
    }

    public Data_Set get_instance_flows() throws Data_Exception {
        return this.get_instance_flows(this.get_set_size());
    }

    public Data_Set get_instance_flows(int number_of_flows) throws Data_Exception {
        if (number_of_flows > this.get_set_size()) {
            throw new Data_Exception("Requested number of paths is too long. Maximum size is " + this.get_set_size() + "!");
        }
        Data_Set set = new Data_Set(this.process_Definition_Id, null);
        while (number_of_flows > 0) {
            String process_Instance_ID = this.instances.get(this.current_number).getId();
            String state = this.instances.get(this.current_number).getState();
            HistoricActivityInstance last_hai = null;
            if (state.contains("TERMINATED")) {
                last_hai = this.get_activity_flow(process_Instance_ID).get(this.get_activity_flow(process_Instance_ID).size() - 1);
            }
            Flow flow = new Flow(process_Instance_ID);
            for (HistoricActivityInstance hai : this.get_activity_flow(process_Instance_ID)) {
                if (this.filter_activity_type(hai.getActivityType())) {
                    if (last_hai != null && hai.getId().equals(last_hai.getId())) {
                        flow.add_Data_Point(new Data_Point(hai, set.get_unique_Task_Name()), true);
                    } else {
                        flow.add_Data_Point(hai, set.get_unique_Task_Name());
                    }
                }
            }
    		
            /*
             *  State extension
             *  activityInstanceState=4,
                ActivityInstanceState DEFAULT = new ActivityInstanceStateImpl(0, "default");
      			ActivityInstanceState SCOPE_COMPLETE = new ActivityInstanceStateImpl(1, "scopeComplete");
      			ActivityInstanceState CANCELED = new ActivityInstanceStateImpl(2, "canceled");
      			ActivityInstanceState STARTING = new ActivityInstanceStateImpl(3, "starting");
      			ActivityInstanceState ENDING = new ActivityInstanceStateImpl(4, "ending");
             */
            set.add_Flow(flow);
            number_of_flows--;
            this.current_number++;
        }
        return set;
    }

    public int get_set_size() {
        return this.instances.size();
    }

    protected boolean filter_activity_type(String type) {
        return !(type.equals("exclusiveGateway") || type.equals("startEvent") || type.equals("endEvent") || type.equals("noneEndEvent"));
    }

    public String get_process_definition_id() {
        return this.process_Definition_Id;
    }

    protected List<HistoricProcessInstance> get_instances() {
        return this.history.createHistoricProcessInstanceQuery()
                .processDefinitionId(this.process_Definition_Id)
                .startedAfter(this.start_date)
                .finishedBefore(this.finished_date)
                .finished()
                .list();

        /*
         * HistoricProcessInstanceEntity[
         * businessKey=null,
         * startUserId=null,
         * superProcessInstanceId=null,
         * rootProcessInstanceId=2bbe66df-7280-11ea-8d0b-505bc2f51738,
         * superCaseInstanceId=null,
         * deleteReason=null,
         * durationInMillis=540380120,
         * startTime=Wed Mar 25 14:15:40 CET 2020,
         * endTime=Tue Mar 31 21:22:00 CEST 2020,
         * removalTime=Thu Apr 30 21:22:00 CEST 2020,
         * endActivityId=invoiceProcessed,
         * startActivityId=StartEvent_1,
         * id=2bbe66df-7280-11ea-8d0b-505bc2f51738,
         * eventType=null,
         * executionId=null,
         * processDefinitionId=invoice:1:f67c8e1a-71a2-11ea-8c5f-505bc2f51738, 		<- BPMN Model Name / Process Instance ID
         * processInstanceId=2bbe66df-7280-11ea-8d0b-505bc2f51738,
         * tenantId=null
         * ]
         */
    }

    protected List<HistoricActivityInstance> get_activity_flow(String process_Instance_ID) {

        return this.history.createHistoricActivityInstanceQuery()
                .processInstanceId(process_Instance_ID)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        /*
         * HistoricActivityInstanceEntity[
         * activityId=assignApprover, 					<- this is the "node_id"
         * activityName=Assign Approver Group, 			<- this is the "node_name"
         * activityType=businessRuleTask,
         * activityInstanceId=null,
         * activityInstanceState=4,
         * parentActivityInstanceId=f90c885c-71a2-11ea-8c5f-505bc2f51738,
         * calledProcessInstanceId=null,
         * calledCaseInstanceId=null,
         * taskId=null,
         * taskAssignee=null,
         * durationInMillis=0,
         * startTime=Tue Mar 24 11:52:16 CET 2020,
         * endTime=Tue Mar 24 11:52:16 CET 2020,
         * eventType=null,
         * executionId=f90c886b-71a2-11ea-8c5f-505bc2f51738,
         * processDefinitionId=invoice:2:f6ebf1c4-71a2-11ea-8c5f-505bc2f51738,
         * rootProcessInstanceId=f90c885c-71a2-11ea-8c5f-505bc2f51738,
         * processInstanceId=f90c885c-71a2-11ea-8c5f-505bc2f51738,
         * tenantId=null
         * ]
         */
    }

}


