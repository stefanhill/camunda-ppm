package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.POST;
import java.util.Map;

public class CopyClassifierResource extends AbstractCockpitPluginResource {

    public CopyClassifierResource(String engineName) {
        super(engineName);
    }

    @POST
    public ClassifierMeta copyClassifier(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        frh.copy_classifier((String) requestData.get("givenName"), (String) requestData.get("copyGivenName"));
        return frh.get_meta_data((String) requestData.get("copyGivenName"));
    }

}