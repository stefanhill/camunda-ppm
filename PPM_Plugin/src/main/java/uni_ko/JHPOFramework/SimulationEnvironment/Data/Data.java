package uni_ko.JHPOFramework.SimulationEnvironment.Data;

import java.util.ArrayList;
import java.util.List;

import uni_ko.JHPOFramework.Structures.Triple;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;

public class Data {
	
	private List<Triple<Data_Set, Data_Set, String>> data = new ArrayList<Triple<Data_Set,Data_Set,String>>();
	
	
	
	public Data(){}
	
	public Data builder() {
		return this;
	}
	public Data addData(Data_Set ds, Double trainingRatio, String name) throws Data_Exception {
		List<Data_Set> l = ds.split(trainingRatio, 1-trainingRatio);
		this.data.add(new Triple<Data_Set, Data_Set, String>(l.get(0), l.get(1), name));
		return this;
	}
	public Data addData(Data_Set train, Data_Set test, String name) throws Data_Exception {
		this.data.add(new Triple<Data_Set, Data_Set, String>(train, test, name));
		return this;
	}
	  
	
	public List<Triple<Data_Set, Data_Set, String>> get_data(){
		return this.data;
	}
	
	
}
