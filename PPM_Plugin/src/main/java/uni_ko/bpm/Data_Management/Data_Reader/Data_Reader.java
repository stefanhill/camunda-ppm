package uni_ko.bpm.Data_Management.Data_Reader;

import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;

public class Data_Reader {
	protected String process_Definition_Id;
	
	public Data_Reader(String process_Definition_Id) {
		this.process_Definition_Id = process_Definition_Id;
	}
	
	public Data_Set get_instance_flows() throws Data_Exception{
		return null;
	}
	public Data_Set get_instance_flows(int number_of_flows) throws Data_Exception{
		return null;
	}
	public int get_set_size() {
		return 0;
	}

	public String get_process_definition_id() {
		return this.process_Definition_Id;
	}

}
