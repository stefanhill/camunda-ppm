package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.runtime;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.PUT;
import java.util.Map;

public class TogglePubliclyClassifierResource extends AbstractCockpitPluginResource {

    public TogglePubliclyClassifierResource(String engineName) {
        super(engineName);
    }

    @PUT
    public void togglePubliclyClassifier(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        Frontend_Request frh = Frontend_Communication_Data.get_frh(processDefinitionId);
        frh.set_public((String) requestData.get("givenName"), (Boolean) requestData.get("publicly"));
    }

}
