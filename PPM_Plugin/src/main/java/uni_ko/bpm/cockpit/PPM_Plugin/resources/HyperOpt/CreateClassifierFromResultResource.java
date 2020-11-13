package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.PUT;
import java.util.Map;


public class CreateClassifierFromResultResource extends AbstractCockpitPluginResource {


    public CreateClassifierFromResultResource(String engineName) {
        super(engineName);
    }

    @PUT
    public void createClassifierFromResult(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);
        h.createClassifier((Integer) requestData.get("classifierId"),
                (String) requestData.get("testName"),
                (String) requestData.get("givenName"),
                (String) requestData.get("author"),
                (Boolean) requestData.get("trained"));
    }
}
