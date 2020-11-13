package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.runtime;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.PUT;
import java.util.List;
import java.util.Map;

public class MergeClassifierResource extends AbstractCockpitPluginResource {

    public MergeClassifierResource(String engineName) {
        super(engineName);
    }

    @PUT
    public void createClassifier(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        Frontend_Request frh = Frontend_Communication_Data.get_frh(processDefinitionId);
        frh.merge_Classifier((String) requestData.get("givenName"),
                (String) requestData.get("author"),
                (List<String>) requestData.get("classifierList"));
    }
}
