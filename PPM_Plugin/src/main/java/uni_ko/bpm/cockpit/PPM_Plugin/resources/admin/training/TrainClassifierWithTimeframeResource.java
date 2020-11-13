package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import javax.ws.rs.PUT;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class TrainClassifierWithTimeframeResource extends AbstractCockpitPluginResource {

    public TrainClassifierWithTimeframeResource(String engineName) {
        super(engineName);
    }


    @PUT
    public Boolean trainClassifierWithTimeframe(Map<String, Object> requestData) throws Exception {
        String resourceName = (String) requestData.get("resourceName");
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data
                .get_frh(processDefinitionId);

        String given_name = (String) requestData.get("givenName");
        Double ratio = (Double) requestData.get("ratio");
        String startDateString = (String) requestData.get("startDate");
        String endDateString = (String) requestData.get("endDate");
        SimpleDateFormat dateParser = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date startDate = dateParser.parse (startDateString);
        Date endDate = dateParser.parse (endDateString);
        frh.asyc_Request( () -> {
            try {
                frh.train(given_name, ratio, startDate, endDate);
            } catch (Exception e) {

            }
        });
        return true;
    }
}