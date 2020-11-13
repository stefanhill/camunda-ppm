package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.runtime;

import org.apache.commons.math3.util.Pair;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.GET;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainedClassifierResource extends AbstractCockpitPluginResource {

    private String resourceName;

    public TrainedClassifierResource(String engineName, String resourceName) {
        super(engineName);
        this.resourceName = resourceName;
    }

    @GET
    public Map<String, List<PredictionType>> getClassifier() throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);
		Map<String, List<PredictionType>> classifierMap = new HashMap<>();
        List<Pair<Classifier_Short_Description, Boolean>> trainedClassifiers = frh.get_classifier(true, true, null);
        for (Pair<Classifier_Short_Description, Boolean> trainedClassifier :
                trainedClassifiers) {
            String givenName = trainedClassifier.getFirst().given_name;
			List<PredictionType> predictionTypes = frh.get_meta_data(givenName).getPredictionTypes();
			classifierMap.put(givenName, predictionTypes);
        }
        return classifierMap;
    }

}
