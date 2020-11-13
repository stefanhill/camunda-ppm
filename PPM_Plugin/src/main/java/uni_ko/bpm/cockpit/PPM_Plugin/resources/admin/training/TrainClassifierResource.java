package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import javax.ws.rs.PUT;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;

import java.util.Map;


public class TrainClassifierResource extends AbstractCockpitPluginResource {

    public TrainClassifierResource(String engineName) {
        super(engineName);

    }

    @PUT
    public Boolean trainClassifier(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        String given_name = (String) requestData.get("givenName");
        double ratio =  (Double)requestData.get("ratio");
        // give training into progress and return
        frh.asyc_Request( () -> {
            try {
                frh.train(given_name, ratio);
            } catch (Exception e) {

            }
        });
        return true;
    }
}