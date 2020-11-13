package uni_ko.bpm.cockpit.PPM_Plugin.resources.prediction;

import java.util.List;

import javax.ws.rs.GET;

import org.apache.commons.math3.util.Pair;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;


public class PublicClassifiersResource extends AbstractCockpitPluginResource {

    private String processDefinitionId;

    public PublicClassifiersResource(String engineName, String processDefinitionId) {
        super(engineName);
        this.processDefinitionId = processDefinitionId;
    }

    @GET
    public List<Pair<Classifier_Short_Description, Boolean>> getPublicClassifiers() throws Exception {
    	Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
        return frh.get_classifier(null, null, true);
    }
}
