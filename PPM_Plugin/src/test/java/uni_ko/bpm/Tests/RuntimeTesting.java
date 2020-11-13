package uni_ko.bpm.Tests;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import uni_ko.bpm.Data_Management.Data_Reader.Data_Reader;
import uni_ko.bpm.Data_Management.Data_Reader.History_Reader;

import java.util.List;

public class RuntimeTesting {

    public static void main(String[] args) {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("CoronaSupermarkt")
                .singleResult();
        System.out.println("Runtime running");
        Data_Reader dr = new History_Reader(processDefinition.getId());
    }
}
