package uni_ko.bpm.Data_Management;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;

import uni_ko.JHPOFramework.Structures.Pair;



public class Data_Point implements Serializable {
    private static final long serialVersionUID = -6347081765393903530L;

    protected HashMap<String, Pair<Class<?>, Object>> dataValues = new HashMap<String, Pair<Class<?>,Object>>();
    
        
    public Data_Point() {}
    public Data_Point(HashMap<String, Pair<Class<?>, Object>> dataValues) {
    	this.dataValues = dataValues;
    }
    public Data_Point(String connceptName) {
    	this.dataValues.put("concept:name", new Pair<Class<?>, Object>(String.class, connceptName));
    }
    public Data_Point(HistoricActivityInstance historic_Activity_Instance, HashMap<String, Integer> unique_Task_Name) {
    	
    	this.dataValues.put("concept:name"	, new Pair<Class<?>, Object>(String.class, historic_Activity_Instance.getActivityId()));
    	if(historic_Activity_Instance.getDurationInMillis() != null) {
    		this.dataValues.put("duration"		, new Pair<Class<?>, Object>(Integer.class, historic_Activity_Instance.getDurationInMillis().intValue()));
    	}else {
    		this.dataValues.put("duration"		, new Pair<Class<?>, Object>(Integer.class, 0));
    	}
    	this.dataValues.put("taskAssignee"	, new Pair<Class<?>, Object>(String.class, historic_Activity_Instance.getAssignee()));

    }
    
    public Data_Point(ActivityInstance activity_Instance, HashMap<String, Integer> unique_Task_Name) {
    	
    	this.dataValues.put("concept:name", new Pair<Class<?>, Object>(String.class, activity_Instance.getActivityName()));
    	this.dataValues.put("duration", new Pair<Class<?>, Object>(Integer.class, 0));
    	this.dataValues.put("taskAssignee", new Pair<Class<?>, Object>(String.class, ""));

    }

    
    
    public void putDataValue(String name, Class<?> type, Object value) {
    	this.dataValues.put(name, new Pair<Class<?>, Object>(type, value));
    }
    
    List<String> dateNames = new ArrayList<String>();
    public void putDateValue(String name, Class<? extends Date> type, Object value) {
    	if(dateNames.contains(name)) {
    		Long duration = Math.abs(((Date)this.dataValues.get(name).getValue()).getTime() - ((Date)value).getTime());
    		if(!this.dataValues.containsKey("duration")) {
    			this.dataValues.put("duration", new Pair<Class<?>, Object>(Integer.class, duration.intValue()));
    		}
    		this.dataValues.put(name+"_Duration", new Pair<Class<?>, Object>(Long.class, duration));
    	}
    	this.dataValues.put(name, new Pair<Class<?>, Object>(type, value));
    	dateNames.add(name);
    }
    
    public HashMap<String, Pair<Class<?>, Object>> getDataValues() {
    	return this.dataValues;
    }
}
