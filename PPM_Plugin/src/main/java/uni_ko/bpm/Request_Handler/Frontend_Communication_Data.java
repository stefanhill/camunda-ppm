package uni_ko.bpm.Request_Handler;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import uni_ko.bpm.Machine_Learning.Classifier;

public class Frontend_Communication_Data {

	public static Long keep_in_memory_ms 	= (long) (10* 60 * 1000); // every 10 minutes
	public static Long check_frequency_ms 	= (long) (5 * 60 * 1000); // every 5 minutes
	
	public static List<String> possible_classifier = null;
	public static List<Class<? extends Classifier>> possible_classifier_classes = null;
	
	public static int maximum_async_executions_per_processDefinitionId = 10;
	
	public static Charset serialization_characterset = StandardCharsets.UTF_8;
	
	public static HashMap<String, Frontend_Request> request_map = new HashMap<String, Frontend_Request>();

	public static Class<? extends Frontend_Request> backend_management = Frontend_Request_Handler.class;
	
	public static Frontend_Request get_frh(String processDefinitionId) throws Exception {
		Frontend_Request requested_Handler;
		if(Frontend_Communication_Data.request_map.containsKey(processDefinitionId)) {
			// process instance already exists
			requested_Handler = (Frontend_Request) Frontend_Communication_Data.request_map.get(processDefinitionId);
			
		}else {
			// create instance
			requested_Handler = Frontend_Communication_Data.backend_management.newInstance();
			requested_Handler.set_PDI(processDefinitionId);

			Frontend_Communication_Data.request_map.put(processDefinitionId, requested_Handler);
		}
		return requested_Handler;
	}
	
}
