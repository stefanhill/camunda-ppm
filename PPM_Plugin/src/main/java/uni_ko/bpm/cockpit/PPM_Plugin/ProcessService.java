package uni_ko.bpm.cockpit.PPM_Plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class ProcessService {

    private static ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    private static RepositoryService repositoryService = processEngine.getRepositoryService();
    
    public static ProcessDefinition getProcessDefinitionByResourceName(String resourceName) {
    	ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionResourceName(resourceName)
                .list()
                .get(0);
        return processDefinition;
    }
}
