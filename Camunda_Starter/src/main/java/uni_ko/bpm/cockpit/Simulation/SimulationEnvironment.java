package uni_ko.bpm.cockpit.Simulation;

import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Creates a simulation setup in the Camunda App to load models and simulate instances
 */
public class SimulationEnvironment {

    protected TaskManagement taskManagement;
    protected ProcessDefinitionManagement definitionManagement;
    protected ProcessInstanceManagement instanceManagement;

    int randMax = 10;
    String decisionVariable = "condition";

    public SimulationEnvironment() {
        this.taskManagement = new TaskManagement();
        this.definitionManagement = new ProcessDefinitionManagement();
        this.instanceManagement = new ProcessInstanceManagement();
    }

    public void buildDefaultSetup(String dir, String key, int numInstances, int numPropagation, double riskFactor) {
        List<Deployment> deployments = this.definitionManagement.deployProcessDefinitionsFromDir(dir);
        ProcessDefinition pd = this.definitionManagement.getProcessDefinitionByKey(key);
        this.simulateInstances(pd, numInstances, riskFactor);
        this.propagateInstances(pd, numPropagation);
        System.out.println("Building simulation finished.");
    }

    public boolean existsSetup(String key) {
        ProcessDefinition pd = this.definitionManagement.getProcessDefinitionByKey(key);
        return pd != null;
    }

    /**
     * Creates a number of instances, that completely traverse the process model
     *
     * @param processDefinition process model
     * @param numInstances      number of instances to simulate
     */
    public void simulateInstances(ProcessDefinition processDefinition, int numInstances, double riskFactor) {
        ProcessInstance[] processInstances = new ProcessInstance[numInstances];
        AtomicInteger j = new AtomicInteger();
        IntStream.range(0, numInstances).parallel().forEach(i -> {
            processInstances[i] = instanceManagement.startInstance(processDefinition.getId(), this.decisionVariable, this.randMax);
            List<Task> tasks = taskManagement.getTasksByInstanceId(processInstances[i].getId());
            while (!tasks.isEmpty()) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (Math.random() < riskFactor) {
                    instanceManagement.deleteProcessByInstanceId(processInstances[i].getId());
                    break;
                } else {
                    instanceManagement.setVariableToRandomValue(processInstances[i].getId(), this.decisionVariable, this.randMax);
                    taskManagement.completeTasksByInstanceId(processInstances[i].getId());
                    tasks = taskManagement.getTasksByInstanceId(processInstances[i].getId());
                }
            }
            j.getAndIncrement();
            System.out.println(processDefinition.getKey() + " - Instance simulation complete: " + j + ":" + i);
        });
    }

    /**
     * Creates a number of instances and lets them randomly traverse and stop at a random task
     *
     * @param processDefinition process model
     * @param numInstances      number of instances to be spawned
     */
    public void propagateInstances(ProcessDefinition processDefinition, int numInstances) {
        ProcessInstance[] processInstances = new ProcessInstance[numInstances];
        AtomicInteger j = new AtomicInteger();
        IntStream.range(0, numInstances).parallel().forEach(i -> {
            processInstances[i] = instanceManagement.startInstance(processDefinition.getId(), this.decisionVariable, this.randMax);
            List<Task> tasks = taskManagement.getTasksByInstanceId(processInstances[i].getId());
            while (Math.random() > 0.2 && !tasks.isEmpty()) {
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instanceManagement.setVariableToRandomValue(processInstances[i].getId(), this.decisionVariable, this.randMax);
                taskManagement.completeTasksByInstanceId(processInstances[i].getId());
                tasks = taskManagement.getTasksByInstanceId(processInstances[i].getId());
            }
            j.getAndIncrement();
            System.out.println(processDefinition.getKey() + " - Propagation simulation complete: " + j + ":" + i);
        });
    }

    public TaskManagement getTaskManagement() {
        return taskManagement;
    }

    public ProcessDefinitionManagement getDefinitionManagement() {
        return definitionManagement;
    }

    public ProcessInstanceManagement getInstanceManagement() {
        return instanceManagement;
    }
}
