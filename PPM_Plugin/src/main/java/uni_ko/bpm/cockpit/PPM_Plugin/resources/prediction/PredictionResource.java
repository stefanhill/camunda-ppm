package uni_ko.bpm.cockpit.PPM_Plugin.resources.prediction;

import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;


public class PredictionResource extends AbstractCockpitPluginResource {

  
  public PredictionResource(String engineName) {
	  super(engineName);
  }

  @POST
  public List<Classification> getPrediction(Map<String, Object> requestData) throws Exception {
	  return (Frontend_Communication_Data.get_frh((String) requestData.get("processDefinitionId"))
              .classify_Instance((String) requestData.get("givenName"), (String) requestData.get("processInstanceId")));
  }
}
