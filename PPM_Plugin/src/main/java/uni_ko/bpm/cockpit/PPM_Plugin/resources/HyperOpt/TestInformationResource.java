package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.POST;
import java.util.List;
import java.util.Map;


public class TestInformationResource extends AbstractCockpitPluginResource {


    public TestInformationResource(String engineName) {
        super(engineName);
    }

    @POST
    public Map<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> getTestInformation(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);
        return h.getConfiguration((String) requestData.get("testName"));
    }
}
