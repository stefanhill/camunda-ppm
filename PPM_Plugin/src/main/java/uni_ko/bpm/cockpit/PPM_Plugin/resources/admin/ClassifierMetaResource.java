package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;


import javax.ws.rs.PUT;
import java.util.Map;


public class ClassifierMetaResource extends AbstractCockpitPluginResource {

    private String resourceName;

    public ClassifierMetaResource(String engineName, String resourceName) {
        super(engineName);
        this.resourceName = resourceName;
    }

    @PUT
    public ClassifierMeta getClassifierMeta(Map<String, Object> requestData) throws Exception {
        String given_name = (String) requestData.get("givenName");
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        ClassifierMeta meta = frh.get_meta_data(given_name);
        // determine last acurracy
        meta.updateAcurracy();
        return meta;
    }
}