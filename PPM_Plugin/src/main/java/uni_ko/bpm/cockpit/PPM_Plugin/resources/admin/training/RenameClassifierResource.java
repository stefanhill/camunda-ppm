package uni_ko.bpm.cockpit.PPM_Plugin.resources.admin.training;

import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginResource;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;
import uni_ko.bpm.Request_Handler.Frontend_Request;
import uni_ko.bpm.cockpit.PPM_Plugin.ProcessService;

import javax.ws.rs.GET;
import java.net.URLDecoder;


public class RenameClassifierResource extends AbstractCockpitPluginResource {

    String resourceName;
    String oldName;
    String newName;

    public RenameClassifierResource(String engineName, String resourceName, String oldName, String newName) {
        super(engineName);
        this.resourceName = resourceName;
        this.oldName =  oldName;
        this.newName = newName;
    }

    @GET
    public ClassifierMeta renameClassifier() throws Exception {
        String processDefinitionId = ProcessService.getProcessDefinitionByResourceName(resourceName).getId();
        Frontend_Request frh = Frontend_Communication_Data.get_frh(processDefinitionId);
        String decodedOldName = URLDecoder.decode(oldName, "utf-8");
        String decodedNewName = URLDecoder.decode(newName, "utf-8");
        frh.rename_classifier(decodedOldName, decodedNewName);
        return frh.get_meta_data(decodedNewName);
    }
}