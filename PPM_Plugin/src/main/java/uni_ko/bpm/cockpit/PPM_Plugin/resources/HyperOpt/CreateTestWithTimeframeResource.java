package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;
import uni_ko.bpm.cockpit.PPM_Plugin.resources.ResourceUtils;

import javax.ws.rs.PUT;
import java.text.SimpleDateFormat;
import java.util.Map;


public class CreateTestWithTimeframeResource extends AbstractCockpitPluginResource {


    public CreateTestWithTimeframeResource(String engineName) {
        super(engineName);
    }

    @PUT
    public boolean createTest(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);

        String startDateString = (String) requestData.get("startDate");
        String endDateString = (String) requestData.get("endDate");
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        h.asyc_Request(() -> {
            try {
                h.createSimulation((String) requestData.get("testName"),
                        ResourceUtils.parseOptimizerConfig(requestData.get("optimizer")),
                        (String) requestData.get("metric"),
                        ResourceUtils.parseClassifierConfig(requestData.get("classifiers")),
                        Integer.valueOf((String) requestData.get("saveNumber")),
                        (Double) requestData.get("ratio"),
                        dateParser.parse(startDateString),
                        dateParser.parse(endDateString));
            } catch (Exception e) {

            }
        });
        return true;
    }
}
