package uni_ko.bpm.cockpit.Simulation;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionManagement {

    protected ProcessEngine processEngine;
    protected RepositoryService repositoryService;

    public ProcessDefinitionManagement() {
        this.processEngine = ProcessEngines.getDefaultProcessEngine();
        this.repositoryService = processEngine.getRepositoryService();
    }

    /**
     * @see uni_ko.bpm.cockpit.Simulation.ProcessDefinitionManagement#deployProcessDefinitionsFromDir(String, boolean) [1]
     * @param dir see [1]
     * @return see [1]
     */
    public List<Deployment> deployProcessDefinitionsFromDir(String dir) {
        return this.deployProcessDefinitionsFromDir(dir, true);
    }

    /**
     * Deploys all bpmn models from a given directory by using
     * @see uni_ko.bpm.cockpit.Simulation.ProcessDefinitionManagement#deployProcessDefinitionFromFile(File, boolean) [1]
     * @param dir the directory for the bpmn files
     * @param removeDuplicates see [1]
     * @return see [1]
     */
    public List<Deployment> deployProcessDefinitionsFromDir(String dir, boolean removeDuplicates) {
        List<Deployment> deployments = new ArrayList<>();
        File directory = new File(dir);
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File modelFile :
                    directoryListing) {
                deployments.add(this.deployProcessDefinitionFromFile(modelFile, removeDuplicates));
            }
        }
        return deployments;
    }

    /**
     * @see uni_ko.bpm.cockpit.Simulation.ProcessDefinitionManagement#deployProcessDefinitionFromFile(File, boolean) [1]
     * with existing models will not be overwritten by default
     * @param modelFile see [1]
     * @return see [1]
     */
    public Deployment deployProcessDefinitionFromFile(File modelFile) {
        return this.deployProcessDefinitionFromFile(modelFile, true);
    }

    /**
     * Deploys a process model from a given file
     * @param modelFile the file as java file object
     * @param removeDuplicates when set to true, the model does not overwrite existing models from the same file
     * @return the deployment as camunda deployment object
     */
    public Deployment deployProcessDefinitionFromFile(File modelFile, boolean removeDuplicates) {
        List<ProcessDefinition> processDefinitions = this.getProcessDefinitions();
        BpmnModelInstance modelInstance = Bpmn.readModelFromFile(modelFile);
        Deployment deployment = repositoryService.createDeployment()
                .addModelInstance(modelFile.getName(), modelInstance)
                .deploy();
        if (removeDuplicates) {
        	try {
	            String deploymentKey = repositoryService.createProcessDefinitionQuery()
	                    .deploymentId(deployment.getId())
	                    .singleResult()
	                    .getKey();
	            for (ProcessDefinition pd :
	                    processDefinitions) {
	                if (pd.getKey().equals(deploymentKey)) {
	                    this.deleteProcessDeploymentById(deployment.getId());
	                    deployment = repositoryService.createDeploymentQuery()
	                            .deploymentId(pd.getDeploymentId())
	                            .singleResult();
	                    break;
	                }
	            }
        	}catch (Exception e) {}
        }
        return deployment;
    }

    /**
     * Queries the camunda api for process definitions
     * @return a list of process definitions
     */
    public List<ProcessDefinition> getProcessDefinitions() {
        return this.repositoryService.createProcessDefinitionQuery()
                .active()
                .list();
    }

    public ProcessDefinition getProcessDefinitionByKey(String key) {
        return this.repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .singleResult();
    }

    public ProcessDefinition getProcessDefinitionFromDeployment(String deploymentId) {
        return this.repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .list()
                .get(0);
    }

    /**
     * deletes all deployments
     */
    public void deleteProcessDeployments() {
        List<Deployment> deployments = this.repositoryService.createDeploymentQuery()
                .list();
        for (Deployment deployment :
                deployments) {
            this.repositoryService.deleteDeployment(deployment.getId());
        }
    }

    /**
     * deletes a deployement by its id
     * @param deploymentId deployment id
     */
    public void deleteProcessDeploymentById(String deploymentId) {
        this.repositoryService.deleteDeployment(deploymentId);
    }

}
