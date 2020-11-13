package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.ResourceUtils;

import javax.ws.rs.PUT;
import java.util.Map;


public class CreateTestResource extends AbstractCockpitPluginResource {


    public CreateTestResource(String engineName) {
        super(engineName);
    }

    @PUT
    public boolean createTest(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);

        h.asyc_Request( () -> {
            try {
                h.createSimulation((String) requestData.get("testName"),
                        ResourceUtils.parseOptimizerConfig(requestData.get("optimizer")),
                        (String) requestData.get("metric"),
                        ResourceUtils.parseClassifierConfig(requestData.get("classifiers")),
                        Integer.valueOf((String) requestData.get("saveNumber")),
                        (Double) requestData.get("ratio"));
            } catch (Exception e) {

            }
        });
        return true;
    }
}
