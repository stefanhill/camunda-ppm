package uni_ko.bpm.Machine_Learning.WekaClassifier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.PredictionType;

public abstract class Weka_Classifier extends Classifier{

	protected int steps;
	protected WekaUtilities wk;
	
    protected List<PredictionType> remapping_data;
    
    
    @Override
    public List<Classification> evaluate(Data_Set data_set) throws Exception {
        return wk.evaluate(data_set, this);
    }
    @Override
    public List<PredictionType> get_prediction_type() {
        return this.prediction_types;
    }
    
	@Override
    public <T> T importSER(String path) throws Exception{
        T classifier = super.importSER(path);

        Weka_Classifier wa = (Weka_Classifier) classifier;
        
        wa.wk.models = new EnumMap<>(PredictionType.class);
        for(int i = 0; i < wa.get_subfiles().size(); i++) {
        	wa.wk.models.put(wa.remapping_data.get(i), this.import_weka(wa.get_subfiles().get(i).getAbsolutePath()));
        }
        return (T) classifier;
    }

	@Override
    public void exportSER(String path) throws Exception {
        this.sub_files = new ArrayList<File>();
        int model_counter = 0;
        
        this.remapping_data = new ArrayList<PredictionType>();
        this.sub_files = new ArrayList<File>();
        
        for(weka.classifiers.Classifier c : this.wk.models.values()) {
        	String sub_path = this.wk.path_manipulator(path, model_counter, this);
        	this.sub_files.add(new File(sub_path));
        	this.export_weka(sub_path, c);
        	model_counter++;
        }
        this.remapping_data = new ArrayList<PredictionType>();
        this.remapping_data.addAll(this.wk.models.keySet());
        super.exportSER(path);
        
    }
	
    private void export_weka(String path, weka.classifiers.Classifier c) throws Exception {
        File check_file = new File(path);
        check_file.getParentFile().mkdirs();
        check_file.createNewFile();
        OutputStream file = new FileOutputStream(path);
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);
        output.writeObject(c);
        output.close();
    }

    private <T> T import_weka(String path) throws Exception {
        InputStream file = new FileInputStream(path);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream(buffer);

        T recovered = (T) input.readObject();
        input.close();

        return recovered;
    }

    
}
