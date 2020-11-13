package uni_ko.bpm.cockpit.Simulation;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;

import java.util.List;

public class TaskManagement {

    protected ProcessEngine processEngine;
    protected TaskService taskService;

    public TaskManagement() {
        this.processEngine = ProcessEngines.getDefaultProcessEngine();
        this.taskService = processEngine.getTaskService();
    }

    public List<Task> getTasks() {
        return this.taskService.createTaskQuery()
                .list();
    }

    public List<Task> getTasksByInstanceId(String pid) {
        return this.taskService.createTaskQuery()
                .processInstanceId(pid)
                .list();
    }

    public void completeTasksByInstanceId(String pid) {
        for (Task t :
                this.getTasksByInstanceId(pid)) {
            this.taskService.complete(t.getId());
        }
    }

}
