package uni_ko.bpm.Data_Management;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;

import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;

import uni_ko.JHPOFramework.Structures.Pair;

public class Flow implements Serializable{

	public List<Data_Point> flow = new ArrayList<Data_Point>();
	protected String process_Instance_ID = null;
	
	protected Data_Point fails_at;
	// Unterscheidung am state
	
	public Flow(String process_Instance_ID) {
		this.process_Instance_ID = process_Instance_ID;
	}
	public Flow(String process_Instance_ID, List<Data_Point> flow) {
		this.process_Instance_ID = process_Instance_ID;
		this.flow = flow;
	}
	
	public void add_Data_Point(HistoricActivityInstance historic_Activity_Instance, HashMap<String, Integer> unique_Task_Name) {
		Data_Point dp = new Data_Point(historic_Activity_Instance, unique_Task_Name);
		flow.add(new Data_Point(historic_Activity_Instance, unique_Task_Name));
	}
	public void add_Data_Point(ActivityInstance activity_Instance, HashMap<String, Integer> unique_Task_Name) {
		flow.add(new Data_Point(activity_Instance, unique_Task_Name));
	}

	
	public void add_Data_Point(Data_Point d_p) {
		if(this.checkMandatoryFields(d_p)) {
			flow.add(d_p);
		}
	}
	public void add_Data_Point(Data_Point d_p, boolean fails) {
		this.add_Data_Point(d_p);
		if(fails) {
			this.fails_at = d_p;
		}
	}
	// remove static
	public static int i = 0;
	public static int j = 0;
	private boolean checkMandatoryFields(Data_Point dp) {
		if(!dp.dataValues.containsKey("duration")) {
			dp.dataValues.put("duration", new Pair<Class<?>, Object>(Integer.class, 0));
		}
		if(dp.dataValues.containsKey("concept:name")) {
			j++;
			return true;
		}
		i++;
		return false;
	}
	
	public List<Data_Point> get_flow(){
		return this.flow;
	}
	public int get_length() {
		return this.flow.size();
	}
	

	/*
	 * Feature Vector generation
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T>  get_flow_parameter(String field_name) 	throws 	IllegalArgumentException, 
																		IllegalAccessException, 
																		NoSuchFieldException, 
																		SecurityException{
		List<T> flow_list = new ArrayList<T>();
		for(Data_Point dp : flow) {
			Pair<Class<?>, Object> pair = dp.dataValues.get(field_name);
			flow_list.add((T) pair.getKey().cast(pair.getValue()));
		}
		return flow_list;
	}
	
	public String get_process_Instance_Id() {return this.process_Instance_ID;}

    /**
     * splits the Flow into n tokens
     *
     * @param token_size set the length of a token
     * @return a list of Flows
     */
    public List<Flow> tokenize_Flow(int token_size) {
        List<Flow> flows = new ArrayList<>();
        List<Data_Point> tempFlow = new ArrayList<>();
        for (int i = 0; i < token_size; i++) {
            tempFlow.add(0, new Data_Point());
        }
        for (int i = 0; i < tempFlow.size() - token_size; i++) {
            Flow f = new Flow(this.get_process_Instance_Id());
            f.set_Flow(tempFlow.subList(i, i + token_size));
            flows.add(f);
        }
        return flows;
    }


    public void set_Flow(List<Data_Point> flow) {
        if (flow != null) {
            this.flow = flow;
        }
    }

	public Data_Point get_Fails_at() 				{	return fails_at;			}

	public void set_Fails_at(Data_Point fails_at) {
		this.fails_at = fails_at;
	}
}
