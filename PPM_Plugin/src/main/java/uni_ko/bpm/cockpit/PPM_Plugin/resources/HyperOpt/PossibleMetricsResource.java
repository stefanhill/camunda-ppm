package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.POST;
import java.util.List;
import java.util.Map;


public class PossibleMetricsResource extends AbstractCockpitPluginResource {


    public PossibleMetricsResource(String engineName) {
        super(engineName);
    }

    @POST
    public List<String> getMetrics(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);
        return h.getAllMetrices();
    }
}
