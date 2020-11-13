package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import java.util.List;

import javax.ws.rs.GET;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

public class AvailableClassifiersResource extends AbstractCockpitPluginResource {

    private String resourceName;

    public AvailableClassifiersResource(String engineName, String resourceName) {
        super(engineName);
        this.resourceName = resourceName;

    }

    @GET
    public List<String> get_classifier_names() throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        return frh.get_possible_classifier();
    }

}
