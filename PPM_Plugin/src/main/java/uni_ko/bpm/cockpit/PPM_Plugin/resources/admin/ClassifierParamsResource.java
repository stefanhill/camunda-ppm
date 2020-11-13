package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin;

import java.util.List;

import javax.ws.rs.GET;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;

import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;


import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;

public class ClassifierParamsResource extends AbstractCockpitPluginResource {

	  private String resourceName;
	  private String classifierName;
	  
	  public ClassifierParamsResource(String engineName, String resourceName, String classifierName) throws Exception {
		  super(engineName);
		  this.resourceName = resourceName;
		  this.classifierName = classifierName;
	  }
	  
	 
	  @GET
	  public List<Parameter_Communication_Wrapper> get_classifier_meta() throws Exception {
		  String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
		  Frontend_Request frh = Frontend_Communication_Data
				  .get_frh(processDefinitionId);
		  return frh.get_classifier_parameter_information(classifierName);
	  }
}