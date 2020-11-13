package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.PUT;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

public class CreateClassifierResource extends AbstractCockpitPluginResource {

    public CreateClassifierResource(String engineName) {
        super(engineName);
    }

    @PUT
    public void createClassifier(Map<String, Object> requestData) throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName((String) requestData.get("resourceName")).getId();
        Frontend_Request frh = Frontend_Communication_Data.get_frh(processDefinitionId);

        List<Map<String, Object>> classifierParamsList = (ArrayList) requestData.get("classifierParams");
        HashMap<Integer, Object> classifierParams = new HashMap<>();
        for (Map<String, Object> value :
                classifierParamsList) {
            Object currentValue = null;
            if (value.get("allow_multi_select") == null) {
                switch (((String) value.get("data_type")).toLowerCase()) {
                    case "java.lang.integer":
                        currentValue = Integer.valueOf((String) value.get("current_value"));
                        break;
                    case "java.lang.double":
                        currentValue = Double.valueOf((String) value.get("current_value"));
                        break;
                    default:
                        currentValue = value.get("current_value");
                        break;
                }
            }
            else {
            	currentValue = value.get("current_value");
			}
            classifierParams.put((Integer) value.get("parameter_id"), currentValue);
        }

        frh.create_classifier((String) requestData.get("givenName"),
                (String) requestData.get("classifierName"),
                classifierParams,
                (String) requestData.get("author"));
    }
}
