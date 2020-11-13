package uni_ko.bpm.Data_Management;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.nd4j.nativeblas.Nd4jCpu.boolean_and;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;
import uni_ko.bpm.Data_Management.XESHandling.XES_Handler;


/**
 * Data set implementation for interacting with camunda log data
 *
 * @author Nico Bartmann
 */
public class Data_Set implements Serializable {
    private static final long serialVersionUID = -4721474018800033818L;

    public List<Flow> data_set = new ArrayList<Flow>();
    public List<DataSetCalculation> postProcessing;
    private HashMap<String, Integer> unique_Task_Name = new HashMap<String, Integer>();

    private boolean isGenerated = false;

    private String process_definition_ID;

    public Data_Set(String process_definition_ID, List<DataSetCalculation> postProcessing) {
        this.process_definition_ID = process_definition_ID;
        this.postProcessing = postProcessing;
        this.postProcess();
    }

    protected Data_Set(List<Flow> data, HashMap<String, Integer> utn, String process_definition_ID, List<DataSetCalculation> postProcessing) {
        this.data_set = data;
        this.process_definition_ID = process_definition_ID;
        this.unique_Task_Name = utn;
    }

    public void add_Flow(Flow flow) {
    	flow = this.postProcess(flow);
        this.data_set.add(flow);
		for(Data_Point dp : flow.flow) {
			String utn = dp.dataValues.get("concept:name").getValue().toString();
			if(!this.unique_Task_Name.containsKey(utn)) {
				this.unique_Task_Name.put(utn, this.unique_Task_Name.size());
			}
		}
    }

    private Flow postProcess(Flow flow) {
    	if(this.postProcessing != null) {
	    	for(DataSetCalculation dsc : this.postProcessing) {
	    		flow = dsc.calculate(flow);
	    	}
	    }
    	return flow;
    }
    public void postProcess() {
    	if(this.postProcessing != null) {
    		for(int index = 0; index < this.data_set.size(); index++) {
		    	for(DataSetCalculation dsc : this.postProcessing) {
		    		Flow f = dsc.calculate(this.data_set.get(index));
		    		this.data_set.set(index, f);
		    	}
		    }
    	}
    }
    public HashMap<String, Integer> get_unique_Task_Name() {
        return this.unique_Task_Name;
    }

    public List<String> get_unique_Tokens() {
        return new ArrayList<>(this.unique_Task_Name.keySet());
    }

    public List<String> get_unique_ids() {
        ArrayList<String> r = new ArrayList<>();
        for (Integer id :
                this.get_unique_Task_Name().values()) {
            r.add(String.valueOf(id));
        }
        return r;
    }
    public int get_flow_count() {
    	return this.data_set.size();
    }
    public String get_process_definition_Id() {
        return this.process_definition_ID;
    }
    public void modify_process_definition_Id(String pdID) {
        this.process_definition_ID = pdID;
    }
    public void set_unique_Tokens(HashMap<String, Integer> unique_Task_Name) {
        this.unique_Task_Name = unique_Task_Name;
    }

    /*
     * Feature Vector generation
     */
    public <T> List<List<T>> get_set_parameter(String field_name) throws IllegalArgumentException,
            IllegalAccessException,
            NoSuchFieldException,
            SecurityException {
    	if(!this.isGenerated) {
    		this.generate();
    	}
        List<List<T>> set = new ArrayList<List<T>>();
        for (Flow f : this.data_set) {
            set.add((List<T>) f.<T>get_flow_parameter(field_name));
        }
        return set;
    }

    /**
     * Merges another data set into the existing one
     *
     * @param set_2           the dataset to be merged
     * @param keep_duplicates specifies whether to keep duplicates
     * @param unsafe_mode     specifies whether to use unsafe mode. If set to true there is no check whether data comes from the same process definition
     * @return the merged data set
     * @throws Data_Exception
     */
    public Data_Set merge_into_new(Data_Set set_2, boolean keep_duplicates, boolean unsafe_mode) throws Data_Exception {
    	if(!this.isGenerated) {
    		this.generate();
    	}
        Data_Set new_set;
        
        List<DataSetCalculation> ls1 = this.postProcessing;
        List<DataSetCalculation> ls2 = set_2.postProcessing;
        ls2.removeAll(ls1);
        ls1.addAll(ls2);
        
        if (this.process_definition_ID != set_2.get_process_definition_Id() &&
                this.process_definition_ID != null &&
                set_2.process_definition_ID != null &&
                !unsafe_mode
        ) {
            throw new Data_Exception("Only processes with the same definition ID can be merged! If you still want to merge use the unsafe mode.");
        } else if (unsafe_mode) {
            new_set = new Data_Set(null, ls1);
        } else {
            new_set = new Data_Set(this.process_definition_ID, ls1);
        }

        List<String> process_Definition_Id_List = new ArrayList<String>();
        for (Flow f : this.data_set) {
            new_set.add_Flow(f);
            process_Definition_Id_List.add(f.get_process_Instance_Id());
        }
        for (Flow f : set_2.data_set) {
            if (keep_duplicates || !(process_Definition_Id_List.contains(f.get_process_Instance_Id()))) {
                new_set.add_Flow(f);
            }
        }
        return new_set;
    }

    /**
     * Splits the data set into a training and a testing data set
     *
     * @param ratio_training   Split ratio for the training set
     * @param ratio_evaluation Split ratio for the testing set
     * @return A list with two data sets {[0]: training, [1]: testing}
     * @throws Data_Exception 
     */
    public List<Data_Set> split(Double ratio_training, Double ratio_evaluation) throws Data_Exception {
    	if(!this.isGenerated) {
    		this.generate();
    	}
    	if(this.data_set.size() < 2) {
    		throw new Data_Exception("Dataset is to short or even empty!");
    	}
        Double rSum = ratio_evaluation + ratio_training;
        if (rSum > 1.0) {
            ratio_training = ratio_training / rSum;
            ratio_evaluation = ratio_evaluation / rSum;
        }
        int trainings_size = (int) Math.ceil(this.data_set.size() * ratio_training);
        int evaluation_size = (int) Math.floor(this.data_set.size() * ratio_evaluation);
        List<Data_Set> return_set = new ArrayList<Data_Set>();
        

        return_set.add(new Data_Set(new ArrayList<Flow>(this.data_set.subList(0, trainings_size - 1)), this.unique_Task_Name, this.process_definition_ID, this.postProcessing));
        return_set.add(new Data_Set(new ArrayList<Flow>(this.data_set.subList(trainings_size, trainings_size+evaluation_size)), this.unique_Task_Name, this.process_definition_ID, this.postProcessing));
        for(Data_Set ds : return_set) {
        	ds.generate();
        }
        return return_set;
    }    
    /**
     * Calculates the longest flow path in the data set
     *
     * @return length of the longest flow as integer
     */
    public int get_Longest_Path() {
        int max_length = 0;
        for (Flow flow : this.data_set) {
            max_length = (flow.get_length() > max_length) ? flow.get_length() : max_length;
        }
        return max_length;
    }
    
    public int get_Shortest_Path() {
        int min_length = Integer.MAX_VALUE;
        for (Flow flow : this.data_set) {
        	min_length = (flow.get_length() < min_length) ? flow.get_length() : min_length;
        }
        return min_length;
    }

    public HashMap<String, Pair<Class<?>, Object>> getUniqueXESTags(){
    	HashMap<String, Pair<Class<?>, Object>> uniqueTags = new HashMap<String, Pair<Class<?>,Object>>();
    	for(Flow flow : this.data_set) {
    		for(Data_Point dp : flow.get_flow()) {
    			uniqueTags.putAll(dp.dataValues);
    		}
    	}
    	return uniqueTags;
    }
    public void generate() {
    	HashMap<String, Pair<Class<?>, Object>> uXesT = this.getUniqueXESTags();
    	for(Flow flow : this.data_set) {
    		for(Data_Point dp : flow.get_flow()) {
    			for(Entry<String, Pair<Class<?>, Object>> entry : uXesT.entrySet()) {
    				if(!dp.dataValues.containsKey(entry.getKey())) {
    					dp.putDataValue(entry.getKey(), entry.getValue().getKey(), null);
    				}
    			}
    		}
    	}
    	this.isGenerated = true;
    }
    /**
     * Imports a local XES event log file
     *
     * @param path Path to the local file
     * @return data set from the XES event log file
     * @throws Data_Exception
     */
    public static Data_Set import_XES(String path, List<DataSetCalculation> postProcessing) throws Data_Exception {
        if (!path.toLowerCase().endsWith(".xes")) {
            throw new Data_Exception("The given path does not end with '.xes'!");
        }
        return XES_Handler.read_XES(path, postProcessing);
    }

    /**
     * Exports a data set to a XES event log
     *
     * @param path Path for the export file
     * @throws Data_Exception
     */
    public void export_XES(String path) throws Data_Exception {
        if (!path.toLowerCase().endsWith(".xes")) {
            throw new Data_Exception("The given path does not end with '.xes'!");
        }
        XES_Handler.write_XES(path, this);
    }

    /**
     * Imports a data set from a local java serialization file
     *
     * @param path Path to the local file
     * @return a data set from the serialization file
     * @throws Data_Exception
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Data_Set import_SER(String path) throws Data_Exception, IOException, ClassNotFoundException {
        if (!path.toLowerCase().endsWith(".ser")) {
            throw new Data_Exception("The given path does not end with '.ser'!");
        }
        InputStream file = new FileInputStream(path);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        Data_Set recovered = (Data_Set) input.readObject();
        input.close();

        return recovered;
    }

    /**
     * Exports a data set to a java serialization file
     *
     * @param path Path to the local file
     * @throws Data_Exception
     * @throws IOException
     */
    public void export_SER(String path) throws Data_Exception, IOException {
        if (!path.toLowerCase().endsWith(".ser")) {
            throw new Data_Exception("The given path does not end with '.ser'!");
        }
        File check_file = new File(path);
        check_file.getParentFile().mkdirs();
        check_file.createNewFile();
        OutputStream file = new FileOutputStream(path);
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);
        output.writeObject(this);
        output.close();
    }
    
	public static Data_Set input_stream2Data_Set(InputStream data_stream, String processDefinitionID) throws IOException {
		File tmp = File.createTempFile("trainings_file_buffer", null);
		FileUtils.copyInputStreamToFile(data_stream, tmp);
		Data_Set ds = XES_Handler.read_XES(tmp, processDefinitionID, null);
		tmp.delete();
		return ds;
	}
	public void setPostProcessing(List<DataSetCalculation> postProcessing) {
        this.postProcessing = postProcessing;
    }

}
