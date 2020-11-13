package uni_ko.bpm.cockpit.PPM_Plugin.resources.HyperOpt;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.JHPOFramework.Communication.CommunicationData;
import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.POST;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PossibleClassifierParameterRangeResource extends AbstractCockpitPluginResource {


    public PossibleClassifierParameterRangeResource(String engineName) {
        super(engineName);
    }

    @POST
    public Map<String, List<Range>> getClassifierParameterRanges(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        CommunicationHandler h = CommunicationData.get_frh(processDefinitionId);
        List<String> classifier = h.getAllClassifier();
        Map<String, List<Range>> classifierParameterRanges = new HashMap<>();
        for (String c :
                classifier) {
            classifierParameterRanges.put(c, h.getNeededRanges(c));
        }
        return classifierParameterRanges;
    }
}
