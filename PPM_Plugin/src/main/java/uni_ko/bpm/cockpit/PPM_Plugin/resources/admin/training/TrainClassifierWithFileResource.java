package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;


import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;


import javax.ws.rs.POST;

import java.io.*;
import java.util.Map;


import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

public class TrainClassifierWithFileResource extends AbstractCockpitPluginResource {

    private String resourceName;


    public TrainClassifierWithFileResource(String engineName, String resourceName) {
        super(engineName);
        this.resourceName = resourceName;
    }


    @POST
    public Boolean trainClassifierWithFile(Map<String, Object> data) throws Exception {
        double training_ratio = (Double) data.get("ratio");
        double evaluation_ratio = 1d - training_ratio;
        String given_name = (String) data.get("givenName");
        String fileContent = (String) data.get("fileContent");
        InputStream dataStream = new ByteArrayInputStream(fileContent.getBytes());

        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data.get_frh(processDefinitionId);
        frh.asyc_Request( () -> {
            try {
                frh.train(given_name, dataStream, training_ratio, evaluation_ratio);
            } catch (Exception e) {
            	
            }
        });
        return true;
    }
}