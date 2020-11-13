package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.runtime;

import org.apache.commons.math3.util.Pair;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.GET;
import java.util.List;

public class AtomicClassifiersResource extends AbstractCockpitPluginResource {

	private String resourceName;
	public AtomicClassifiersResource(String engineName, String resourceName) {
		super(engineName);
		this.resourceName = resourceName;

	}
	  
	  @GET
	  public List<Pair<Classifier_Short_Description, Boolean>> getClassifier() throws Exception {
		  String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
		  Frontend_Request frh = Frontend_Communication_Data
				  .get_frh(processDefinitionId);
		  return frh.get_classifier(true, true, null);
	  }

}
