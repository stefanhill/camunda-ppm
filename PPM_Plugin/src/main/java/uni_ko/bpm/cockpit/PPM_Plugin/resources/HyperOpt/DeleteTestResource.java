package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.PUT;
import java.util.Map;


public class DeleteTestResource extends AbstractCockpitPluginResource {


    public DeleteTestResource(String engineName) {
        super(engineName);
    }

    @PUT
    public void deleteTest(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);
        h.deleteSimulation((String) requestData.get("testName"));
    }
}
