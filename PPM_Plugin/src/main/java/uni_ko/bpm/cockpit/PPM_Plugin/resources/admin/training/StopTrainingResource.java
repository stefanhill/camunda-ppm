package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import javax.ws.rs.GET;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import java.net.URLDecoder;

public class StopTrainingResource extends AbstractCockpitPluginResource {

    private String resourceName;
    private String givenName;

    public StopTrainingResource(String engineName, String resourceName, String givenName) {
        super(engineName);
        this.resourceName = resourceName;
        this.givenName = givenName;
    }

    @GET
    public boolean stop_training() throws Exception {
        String decoded_given_name = URLDecoder.decode(givenName, "utf-8");
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        frh.stop_training(decoded_given_name);
        return true;

    }

}
