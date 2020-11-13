package uni_ko.bpm.cockpit.PPM_Plugin.resources.prediction;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;

import javax.ws.rs.GET;
import java.net.URLDecoder;
import java.util.List;

public class ClassifierPredictionTypesResource extends AbstractCockpitPluginResource {

	  private String processDefinitionId;
	  private String givenName;

	  public ClassifierPredictionTypesResource(String engineName, String processDefinitionId, String givenName) {
		  super(engineName);
		  this.processDefinitionId = processDefinitionId;
		  this.givenName = givenName;
	  }
	  
	 
	  @GET
	  public List<PredictionType> getPredictionTypes() throws Exception {
		  String decoded_given_name = URLDecoder.decode(givenName, "utf-8");
		  Frontend_Request frh = Frontend_Communication_Data
				  .get_frh(this.processDefinitionId);
		  return frh.get_prediction_type(decoded_given_name, true);
	  }
}