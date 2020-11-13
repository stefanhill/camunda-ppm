package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin;

import java.net.URLDecoder;
import java.util.List;

import javax.ws.rs.GET;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;

public class ClassifierInstanceParamsResource extends AbstractCockpitPluginResource {

    private String resourceName;
    private String givenName;

    public ClassifierInstanceParamsResource(String engineName, String resourceName, String givenName) {
        super(engineName);
        this.resourceName = resourceName;
        this.givenName = givenName;	}

    @GET
    public List<Parameter_Communication_Wrapper> get_classifier_instance_params() throws Exception {
        String decoded_given_name = URLDecoder.decode(givenName, "utf-8");
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data.get_frh(processDefinitionId);
        List<Parameter_Communication_Wrapper> params = frh.get_classifier_current_parameter_information(decoded_given_name);
        return params;
    }
}
