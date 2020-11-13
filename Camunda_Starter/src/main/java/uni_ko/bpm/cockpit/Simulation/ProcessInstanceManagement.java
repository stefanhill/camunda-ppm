package uni_ko.bpm.cockpit.Simulation;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.List;

public class ProcessInstanceManagement {

    protected ProcessEngine processEngine;
    protected RuntimeService runtimeService;

    public ProcessInstanceManagement() {
        this.processEngine = ProcessEngines.getDefaultProcessEngine();
        this.runtimeService = processEngine.getRuntimeService();
    }

    public ProcessInstance startInstance(String definitionId) {
        return this.runtimeService.startProcessInstanceById(definitionId);
    }

    public ProcessInstance startInstance(String definitionId, String variable, double randMax) {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put(variable, Math.floor(Math.random() * randMax));
        return this.runtimeService.startProcessInstanceById(definitionId, variables);
    }

    public void setVariableToRandomValue(String instanceId, String variable, int randMax) {
        Double value = Math.floor(Math.random() * randMax);
        runtimeService.setVariable(instanceId, variable, value);
    }

    public List<ProcessInstance> getProcessInstances() {
        return this.runtimeService.createProcessInstanceQuery()
                .list();
    }

    public List<ProcessInstance> getProcessInstances(String did) {
        return this.runtimeService.createProcessInstanceQuery()
                .deploymentId(did)
                .list();
    }

    public void deleteProcessInstances() {
        List<ProcessInstance> processInstances = this.runtimeService.createProcessInstanceQuery()
                .list();
        for (ProcessInstance processInstance :
                processInstances) {
            this.runtimeService.deleteProcessInstance(processInstance.getId(), "---");
        }
    }

    public void deleteProcessByInstanceId(String pid) {
        this.runtimeService.deleteProcessInstance(pid, "---");
    }

}
