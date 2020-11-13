package uni_ko.JHPOFramework.Communication;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;


public class CommunicationData {
	public static String defaultPath = System.getProperty("user.dir") + File.separator + "Simulations" + File.separator;
	
	public static List<String> possible_classifier = null;
	
	public static int maximum_async_executions_per_processDefinitionId = 10;
	
	public static Charset serialization_characterset = StandardCharsets.UTF_8;
	
	public static HashMap<String, CommunicationHandler> request_map = new HashMap<String, CommunicationHandler>();

	public static Class<? extends CommunicationHandler> backend_management = CommunicationHandler.class;
	
	public static CommunicationHandler get_frh(String processDefinitionId) throws Exception {
		processDefinitionId = URLEncoder.encode(processDefinitionId, Frontend_Communication_Data.serialization_characterset.toString());
		CommunicationHandler requested_Handler;
		if(CommunicationData.request_map.containsKey(processDefinitionId)) {
			// process instance already exists
			requested_Handler = (CommunicationHandler) CommunicationData.request_map.get(processDefinitionId);
			
		}else {
			// create instance
			requested_Handler = CommunicationData.backend_management.newInstance();
			requested_Handler.set_PDI(processDefinitionId);

			CommunicationData.request_map.put(processDefinitionId, requested_Handler);
		}
		return requested_Handler;
	}
}
