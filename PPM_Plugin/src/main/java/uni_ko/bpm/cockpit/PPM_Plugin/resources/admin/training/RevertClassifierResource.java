package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import javax.ws.rs.POST;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import java.util.Map;

public class RevertClassifierResource extends AbstractCockpitPluginResource {

    public RevertClassifierResource(String engineName) {
        super(engineName);
    }

    @POST
    public ClassifierMeta revertClassifier(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        frh.revert_classifier((String) requestData.get("givenName"), (Integer) requestData.get("targetVersion"));
        return frh.get_meta_data((String) requestData.get("givenName"));
    }

}