package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.POST;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PossibleOptimizerResource extends AbstractCockpitPluginResource {


    public PossibleOptimizerResource(String engineName) {
        super(engineName);
    }

    @POST
    public HashMap<String, List<Parameter_Communication_Wrapper>> getOptimizer(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);
        return h.getAllOptimizer();
    }
}
