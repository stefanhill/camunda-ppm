package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import java.util.List;

import javax.ws.rs.GET;

import org.apache.commons.math3.util.Pair;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;


public class TrainableClassifiersResource extends AbstractCockpitPluginResource {

    private String resourceName;

    public TrainableClassifiersResource(String engineName, String resourceName) {
        super(engineName);
        this.resourceName = resourceName;
    }

    @GET
    public List<Pair<Classifier_Short_Description, Boolean>> getTrainableClassifiers() throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        return Frontend_Communication_Data
                .get_frh(processDefinitionId)
                .get_classifier(true, null, null);
    }
}