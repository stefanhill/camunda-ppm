package uni_ko.bpm.Request_Handler;

import org.apache.commons.math3.util.Pair;
import org.nd4j.shade.guava.collect.ImmutableSet;
import org.nd4j.shade.guava.reflect.ClassPath;
import org.nd4j.shade.guava.reflect.ClassPath.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Reader.Data_Reader;
import uni_ko.bpm.Data_Management.Data_Reader.History_Reader;
import uni_ko.bpm.Data_Management.Data_Reader.Running_Reader;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.*;
import uni_ko.bpm.Machine_Learning.Util.Classifier_Short_Description;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Reflections.ReflectionReads;
import uni_ko.bpm.cockpit.PPM_Plugin.CockpitPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class Frontend_Request_Handler implements Frontend_Request {
	//  	 	   given_name   <classifier, Date>
	private HashMap<String, Pair<Classifier, Date>> classifier_map;
	private HashMap<String, Classifier> untrained_classifier_map;

	private Classifier 	default_classifier;
	private String 		default_classifier_given_name;

	private Logger logger = LoggerFactory.getLogger(Frontend_Request_Handler.class);
	private volatile List<String> active_training = new ArrayList<String>();
	
	private ExecutorService async_request = Executors.newFixedThreadPool(Frontend_Communication_Data.maximum_async_executions_per_processDefinitionId);
	private Map<String, TrainingStatus> training_status_map = new HashMap<>();
	
	private String process_Definition_Id;
	
	private String processKey;

	private Thread memory_limiter;



	public enum Folder {
		Trained("trained"		,"trained/"),
		Historic("historic"		,"historic/"),
		Meta("meta"				,"meta/"),
		Default("default"		,"default/");
		public final String folder_str;
		public final String folder_path;

		Folder(String folder_str, String folder_path) {
			 this.folder_str = folder_str;
			 this.folder_path = folder_path;
	    }
	}

	public Frontend_Request_Handler() {}

	public void set_PDI(String processDefinitionId) throws Exception {
		this.process_Definition_Id = processDefinitionId;
		this.processKey = this.getProcessKey(processDefinitionId);
		this.classifier_map = new HashMap<String,Pair<Classifier, Date>>();
		this.untrained_classifier_map = new HashMap<String,Classifier>();

		this.default_classifier = this.deserialize_default_classifier();
		if(!(this.default_classifier == null)) {
			 this.default_classifier_given_name = this.default_classifier.get_given_name();
		}
		
		this.log_message("Frontend-Request-Handler for " + processDefinitionId + " created.");
		
		this.set_memory_limiter();
		this.memory_limiter.start();
	}


	private void set_memory_limiter() {
		this.log_message("Memory-Handler for " + this.process_Definition_Id + " created.");
		this.memory_limiter = new Thread(new Runnable()
		{
		    @Override
		    public void run()
		    {
		    	while(!memory_limiter.isInterrupted()) {
		    		if(classifier_map.size() != 0) {
				        Iterator<Entry<String, Pair<Classifier, Date>>> it = classifier_map.entrySet().iterator();
				        while (it.hasNext()) {
				            Map.Entry<String, Pair<Classifier, Date>> entry = it.next();
				            Date last_used = entry.getValue().getSecond();
				            if((new Date()).toInstant().toEpochMilli() - last_used.toInstant().toEpochMilli() > Frontend_Communication_Data.keep_in_memory_ms &&
				            		!active_training.contains(entry.getKey())
				            ) {
				            	try {
				            		Classifier c = entry.getValue().getFirst();
									save_Classifier(c);
									log_message("Remove Classifier " +c.get_given_name() + " from memory.");
									it.remove();
								} catch (Exception e) {
									e.printStackTrace();
								}
				            }
				        }
				        try {
				        	memory_limiter.sleep(Frontend_Communication_Data.check_frequency_ms);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
		    		}
		    	}
		    }
		});
	}

	private String getProcessKey(String processDefinitionId) {
		String[] subStrings = processDefinitionId.split(":");
		int processKeyPrefixEnd = subStrings.length - 2;
		String processKey = "";
		for( int i = 0; i < processKeyPrefixEnd; i++){
			processKey += subStrings[i];
		}
		return processKey;
	}


/*
 * CREATION HANDLING
 */
	
	public List<String> get_possible_classifier() throws Exception{
		return ReflectionReads.getPossibleClassifier();
	}
	
	

	@Override
	public void merge_Classifier(String new_given_name, String author, List<String> given_names) throws Exception {
		List<Classifier> classifiers = new ArrayList<>();

		for(String given_name : given_names) {
			classifiers.add(this.get_classifier_by_given_name(given_name));
		}
		HashMap<Integer, Object> parameters = new HashMap<>();
		parameters.put(0, classifiers);

		this.create_classifier(new_given_name, "Merged_Classifier", parameters, author);
		
		String c_name = classifiers.stream().map(Classifier::get_given_name).collect(Collectors.joining(",", "{", "}"));
		this.log_message("New 'Merged Classifier' " + new_given_name + " was created by " + author + " from the following classifiers: " + c_name + ".");
	}
	
	public void create_classifier(String given_name, String classifier_name, HashMap<Integer, Object> parameters, String author) throws Exception{
		if(has_name_collision(given_name)) {
			this.log_error("The given name " + given_name + " is already taken and has to be unique.", Communication_Exception.class);
		}
		
		Classifier new_classifier = ReflectionReads.getInstanceByName(classifier_name);
		new_classifier.set_configurational_parameters(parameters);

		int version = 0;
		if(!new_classifier.is_trainable()) {
			version = 1;
		}
		Boolean is_publicly_available = false;

		ClassifierMeta classifier_meta = new ClassifierMeta(
											given_name,
											version,
											new_classifier.get_prediction_type(),
											new Date(),
											new Date(),
											author,
											is_publicly_available,
											new_classifier
											);

		new_classifier.set_Meta_Data(classifier_meta);
		
		if(new_classifier.is_trainable()) {
			this.untrained_classifier_map.put(given_name, new_classifier);
		}else {
			this.classifier_map.put(given_name, Pair.create(new_classifier, new Date()));
			this.save_Classifier(new_classifier, Folder.Trained);
		}
		this.log_message("New classifier " + classifier_name + " has been created.");
	}
	
	private void check_given_name(String given_name) {
		
	}
	private boolean has_name_collision(String given_name) throws Exception  {
		
		if(default_classifier_given_name != null && default_classifier_given_name == given_name) {
			return true;
		}else if(this.get_classifier_path(given_name) != null) {
			return true;
		}else if(this.get_historic_classifier_path_list(given_name).size() > 0) {
			return true;
		}
		return false;
	}
	@Override
	public List<Pair<Classifier_Short_Description, Boolean>> get_classifier(Boolean get_trainable, Boolean already_trained, Boolean is_public) throws Exception{
		// use a set instead of a ArrayList to ensure there are no duplicates
		Set<Pair<Classifier_Short_Description, Boolean>> ret_set = new LinkedHashSet<Pair<Classifier_Short_Description,Boolean>>();
		Set<ClassifierMeta> meta_list = new LinkedHashSet<ClassifierMeta>();

		// default handler
		if(this.default_classifier != null) {
			if(this.gt_3(true, already_trained) && this.gt_3(this.default_classifier.is_trainable(), get_trainable) && this.gt_3(true, is_public)) {
				ret_set.add(Pair.create(this.default_classifier.get_Meta_Data().getShort_description(), true));
			}
		}

		// basic handling
		if(already_trained == null || already_trained) {
			meta_list.addAll(this.classifier_map.values().stream()
					.map(c -> c.getFirst().get_Meta_Data())
					.collect(Collectors.toList()));
			List<ClassifierMeta> duplicate_list = this.deserialize_meta_from_folder(this.get_local_path()+Folder.Trained.folder_path, this.classifier_map.keySet().stream().collect(Collectors.toList()));
			for (ClassifierMeta meta : duplicate_list){
				/*if(meta == null){
					// hotfix ignore older versions (changed serial uuid)
					continue;
				}*/
				boolean hasDuplicate = false;
				for (ClassifierMeta meta2 : meta_list){
					/*if(meta2 == null){
						// hotfix ignore older versions (changed serial uuid)
						continue;
					}*/
					if (meta2.get_given_name().equals(meta.get_given_name())){
						hasDuplicate = true;
						break;
					}
				}
				if(!hasDuplicate){
					meta_list.add(meta);
				}
			}
		}
		if(already_trained == null || !already_trained)  {
			meta_list.addAll(this.untrained_classifier_map.values().stream().map(Classifier::get_Meta_Data).collect(Collectors.toList()));
		}
		ret_set.addAll(meta_list.stream()
					.filter(m -> !m.getShort_description().given_name.equals(this.default_classifier_given_name))
					.filter(m -> this.gt_3(m.isIs_trainable(), get_trainable))
					.filter(m -> this.gt_3(m.getIs_publicly_available(), is_public))
					.map(m ->  Pair.create(m.getShort_description(), false))
					.collect(Collectors.toList())
				);
		return new ArrayList<Pair<Classifier_Short_Description,Boolean>>(ret_set);
	}
	private boolean gt_3(Boolean bigger, Boolean than) {
		if(bigger == null) {
			return false;
		} else if(than == null) {
			return true;
		} else if(bigger.equals(than)) {
			return true;
		}
		return false;
	}
	public List<Parameter_Communication_Wrapper> get_classifier_parameter_information(String classifier_name) throws Exception {
		Classifier classifier = ReflectionReads.getInstanceByName(classifier_name);
		return classifier.configurational_parameters();
	}
	public List<Parameter_Communication_Wrapper> get_classifier_current_parameter_information(String given_name) throws Exception {
		Classifier classifier = this.get_classifier_by_given_name(given_name);
		return classifier.configurational_parameters();
	}


/*
 * 	PATH HANDLING
 */
	private String get_local_path() {
		return CockpitPlugin.resourcePath + "/classifier/" + this.processKey + "/";
	}
	private String get_meta_path(String given_name, String version) throws Exception  {
		String meta_path = this.get_local_path() + Folder.Meta.folder_path + "meta-file-" + this.encode(given_name) + "-" + version +".ser";
		return meta_path;
	}
	private String get_classifier_path(String given_name) throws Exception  {
		if(this.default_classifier != null && this.default_classifier.get_given_name() == given_name) {
			return this.get_classifier_path(given_name, Folder.Default, null);
		}else {
			return this.get_classifier_path(given_name, Folder.Trained, null);
		}
	}
	private String get_classifier_path(String given_name, String version) throws Exception  {
		if(this.default_classifier != null && this.default_classifier.get_given_name() == given_name) {
			return this.get_classifier_path(given_name, Folder.Default, version);
		}else {
			String path = this.get_classifier_path(given_name, Folder.Trained, version);
			if(path != null) {
				return path;
			}
			return this.get_classifier_path(given_name, Folder.Historic, version);
		}
	}
	private String get_classifier_path(String given_name, Folder folder, String version) throws Exception  {
		File location = new File((this.get_local_path() + folder.folder_path));
		String storage_given_name = this.encode(given_name);
		if(location.listFiles() != null) {
			for(File f : location.listFiles()) {
				if(f.isFile()) {
					if(this.get_given_name(f).equals(storage_given_name) && (version == null || this.get_version(f).equals(version))) {
						return f.getAbsolutePath();
					}
				}
			}
		}
		return null;
	}
	private List<String> get_historic_classifier_path_list(String given_name) {
		List<String> re = new ArrayList<String>();
		File folder = new File(this.get_local_path() + Folder.Historic.folder_path);
		File[] files = folder.listFiles();
		if(files == null) {
			return re;
		}
		for(File f : files) {
			if(f.isFile() && this.get_given_name(f) == given_name) {
				re.add(f.getAbsolutePath());
			}
		}
		return re;
	}
	private String build_classifier_name(Classifier classifier) throws Exception  {
		return 	classifier.get_Classifier_Name() + "$" + 
				this.encode(classifier) + "$" + 
				classifier.get_version() + "$" + 
				".ser";
	}

	private String encode(Classifier classifier) throws Exception  {
		try {
			return URLEncoder.encode(classifier.get_given_name(), Frontend_Communication_Data.serialization_characterset.toString());
		} catch (UnsupportedEncodingException e) {
			this.log_error("Encoding misbehaviour.", UnsupportedEncodingException.class);
		}	
		return null;
	}
	private String encode(String s) throws Exception {
		try {
			return URLEncoder.encode(s, Frontend_Communication_Data.serialization_characterset.toString());
		} catch (UnsupportedEncodingException e) {
			this.log_error("Encoding misbehaviour.", UnsupportedEncodingException.class);
		}		
		return null;
	}
	private String decode(String s) throws Exception  {
		try {
			return URLDecoder.decode(s, Frontend_Communication_Data.serialization_characterset.toString());
		} catch (UnsupportedEncodingException e) {
			this.log_error("Decoding misbehaviour.", UnsupportedEncodingException.class);
		}
		return null;
	}


/*
 * 	FILE HANDLING
 */
	private void move(Classifier classifier, Folder from_folder, Folder to_folder) throws Exception {
		String classifier_path = this.get_classifier_path(classifier.get_given_name(), from_folder, null);
		if(classifier.get_subfiles() != null) {
			for(File f : classifier.get_subfiles()) {
				this.delete_file(f);
			}
		}
		this.delete_file(new File(classifier_path));
		// reserialize
		this.save_Classifier(classifier, to_folder);
	}
	private void move(Classifier classifier, Folder from_folder, Folder to_folder, Integer version) throws Exception {
		String classifier_path = this.get_classifier_path(classifier.get_given_name(), from_folder, Integer.toString(version));
		// remove all files
		if(classifier.get_subfiles() != null) {
			for(File f : classifier.get_subfiles()) {
				this.delete_file(f);
			}
		}
		this.delete_file(new File(classifier_path));
		// reserialize
		this.save_Classifier(classifier, to_folder);
	}
	private void move_and_create(String from, String to) throws IOException {
		
		(new File(from)).getParentFile().mkdirs();
		(new File(to)).getParentFile().mkdirs();
		Files.move(Paths.get(from), Paths.get(to));
	}
	private void move(Classifier classifier, Folder to_folder) throws Exception {
		this.move(classifier, this.get_Folder(classifier), to_folder);
	}
	private void delete_file(File to_delete) {
		to_delete.delete();
	}
	private String get_classifier_name(File file) {
		return (file.getName().split("\\$"))[0];
	}
	private String get_given_name(File file) {
		return (file.getName().split("\\$"))[1];
	}
	private String get_version(File file) {
		return (file.getName().split("\\$"))[2];
	}

	private Folder get_Folder(Classifier classifier) throws Exception  {
		String path = this.get_classifier_path(classifier.get_given_name(), String.valueOf(classifier.get_version()));
		path = path.replace("\\","/");
		if(path.contains(Folder.Historic.folder_path)) {
			return Folder.Historic;
		} else if(path.contains(Folder.Trained.folder_path)) {
			return Folder.Trained;
		} else if(path.contains(Folder.Default.folder_path)) {
			return Folder.Default;
		}

		return null;
	}


/*
 * DESERIALIZATION
 */
	private List<Classifier> deserialize_all_classifier(Folder folder) throws Exception{
		return this.deserialize_Classifier_from_folder(this.get_local_path()+folder.folder_path );
	}
	private List<ClassifierMeta> deserialize_all_trained_meta(List<String> except, Folder folder) throws Exception{
		return this.deserialize_meta_from_folder(this.get_local_path()+folder.folder_path, except );
	}
	private List<Classifier> deserialize_Classifier_from_folder(String path) throws Exception{
		List<Classifier> list = new ArrayList<Classifier>();
		File[] files = new File(path).listFiles();
		if(!(files == null)) {
			for(File file : files) {
			    if(file.getAbsolutePath().endsWith(".ser")){
				Classifier classifier = this.deserialize_Classifier(file);
				list.add(classifier);
			    }
			}
		}
		return list;
	}
	private List<ClassifierMeta> deserialize_meta_from_folder(String path, List<String> given_names_exception) throws Exception{
		File folder = new File(path);
		List<ClassifierMeta> list = new ArrayList<ClassifierMeta>();
		File[] files = folder.listFiles();
		if(!(files == null)) {
			for(File file : files) {
				if(file.getAbsolutePath().endsWith(".ser") && !given_names_exception.contains(this.get_given_name(file))) {
					ClassifierMeta meta = this.deserialize_meta_data(file);
					list.add(meta);
				}
			}
		}
		return list;
	}
	private Classifier deserialize_Classifier(File classifier_file) throws Exception {
		Classifier classifier = ReflectionReads.deserializeClassifier(classifier_file, this.get_classifier_name(classifier_file));

		ClassifierMeta meta = this.deserialize_meta_data(classifier_file);
		classifier.set_Meta_Data(meta);
		this.log_message("Deserialization of Classifier " + classifier.get_given_name() + ".");
		return classifier;
	}
	private ClassifierMeta deserialize_meta_data(File classifier_file) throws Exception {
		return ClassifierMeta.import_SER(this.get_meta_path(this.get_given_name(classifier_file), this.get_version(classifier_file)));
	}
	private Classifier deserialize_by_given_name(String given_name) throws Exception {
		return this.deserialize_Classifier(new File(this.get_classifier_path(given_name)));
	}
	private Classifier deserialize_default_classifier() throws Exception {
		List<Classifier> classifier_list = this.deserialize_Classifier_from_folder(this.get_local_path()+Folder.Default.folder_path );
		if(classifier_list.size() > 1) {
			this.log_error("Too many default Classifier found!", Communication_Exception.class);
		}
		if(classifier_list.size() == 0) {
			return null;
		}
		return classifier_list.get(0);
	}



/*
 * MANAGEMENT HANDLING
 */

	@Override
	public void delete_classifier(String given_name) throws Exception {
		Classifier classifier = this.get_classifier_by_given_name(given_name);
		this.delete_classifier_version(given_name, String.valueOf(classifier.get_version()));
	}

	public void delete_classifier_version(String given_name, String version) throws Exception {
		if(Integer.valueOf(version) == 0) {
			this.untrained_classifier_map.remove(given_name);
			this.log_message("The classifier " + given_name + " has been deleted, before it was even trained...");
		}else {
			Classifier classifier = this.get_classifier_by_gn_and_v(given_name, version);
			this.delete_classifier(classifier);
			// delete previous
			for(int v = classifier.get_version()-1; v >= 1; v--) {
				this.delete_classifier(this.get_classifier_by_gn_and_v(given_name, Integer.toString(v)));
			}
			this.log_message("The classifier " + given_name + " has been deleted, including all versions.");
		}
	}
	private void delete_classifier(Classifier classifier) throws Exception {
		String given_name = classifier.get_given_name();
		if(classifier_map.containsKey(given_name)) {
			classifier_map.remove(given_name);
		}else if(this.default_classifier_given_name != null && this.default_classifier_given_name.equals(given_name)){
			this.default_classifier = null;
			this.default_classifier_given_name = null;
			if(classifier_map.containsKey(given_name)) {
				classifier_map.remove(given_name);
			}
		}else if(this.untrained_classifier_map.containsKey(given_name)) {
			this.untrained_classifier_map.remove(given_name);
		}
		if(classifier.get_subfiles() != null) {
			for(File file : classifier.get_subfiles()) {
				this.delete_file(file);
			}
		}
		this.delete_file(new File(this.get_meta_path(classifier.get_given_name(), Integer.toString(classifier.get_version()))));
		this.delete_file(new File(this.get_classifier_path(classifier.get_given_name(), this.get_Folder(classifier) , Integer.toString(classifier.get_version()))));
	}
	@Override
	public void add_Classifier(Classifier classifier) throws Exception{
		this.save_Classifier(classifier, Folder.Trained);
	}
	private void save_Classifier(Classifier classifier) throws Exception{
		this.save_Classifier(classifier, this.get_Folder(classifier));
	}
	private void save_Classifier(Classifier classifier, Folder folder) throws Exception {
		String path = this.get_local_path() + folder.folder_path + this.build_classifier_name(classifier);
		File classifier_file = new File(path);
		classifier.exportSER(path);
		classifier.get_Meta_Data().export_SER(this.get_meta_path(this.get_given_name(classifier_file), this.get_version(classifier_file)));
		this.log_message("Classifier " + classifier.get_given_name() +" has beend saved.");
	}
	public void set_new_default(String given_name) throws Exception {
		if (this.default_classifier != null) {
			Classifier old_default = this.default_classifier;
			this.move(old_default, Folder.Default, Folder.Trained);
			this.classifier_map.put(old_default.get_given_name(), Pair.create(old_default, new Date()));
			this.log_message(old_default.get_given_name() + " has had its default status revoked.");
		}
		if(given_name == null){
			return;
		}

		Classifier new_default = this.get_classifier_by_given_name(given_name);
		this.move(new_default, Folder.Trained, Folder.Default);
		new_default.get_Meta_Data().setIs_publicly_available(true);

		this.default_classifier = new_default;
		this.default_classifier_given_name = given_name;
		
		this.log_message("Set " + given_name + "  to new default.");
	}
	public void set_public(String given_name, boolean is_publicly_available) throws Exception{
		if(this.default_classifier_given_name != null && this.default_classifier_given_name.equals(given_name)) {
			this.log_error("A default classifier has to be publicly accesible!", UnsupportedOperationException.class);
		}

		Classifier c = this.get_classifier_by_given_name(given_name);
		c.get_Meta_Data().setIs_publicly_available(is_publicly_available);
		
		this.log_message("Change " + given_name + " to new publicity setting: " + is_publicly_available);
		this.save_Classifier(c);
	}
	public Classifier get_classifier_by_given_name(String given_name) throws Exception {
		Classifier classifer;
		if(this.default_classifier_given_name != null && this.default_classifier_given_name.equals(given_name)) {
			classifer = this.default_classifier;
		}else if(this.classifier_map.containsKey(given_name)) {
			classifer = this.classifier_map.get(given_name).getFirst();
		}else if(this.untrained_classifier_map.containsKey(given_name)) {
			classifer = this.untrained_classifier_map.get(given_name);
		}else {
			classifer = this.deserialize_by_given_name(given_name);
			this.classifier_map.put(given_name, Pair.create(classifer, new Date()));
		}
		return classifer;
	}
	private Classifier get_classifier_by_gn_and_v(String given_name, String version) throws Exception {
		Classifier classifer;
		if(this.default_classifier_given_name != null && this.default_classifier_given_name.equals(given_name)) {
			classifer = this.default_classifier;
		}else if(this.classifier_map.containsKey(given_name) && this.classifier_map.get(given_name).getFirst().get_version() == Integer.valueOf(version)) {
			classifer = this.classifier_map.get(given_name).getFirst();
		}else if(version.equals("0") && this.untrained_classifier_map.containsKey(given_name)) {
			classifer = this.untrained_classifier_map.get(given_name);
		}else {
			classifer = this.deserialize_Classifier(new File(this.get_classifier_path(given_name, version)));
			this.classifier_map.put(given_name, Pair.create(classifer, new Date()));
		}
		return classifer;
	}



	public List<Classification> classify_Instance(String given_name, String process_Instance_Id) throws Exception {
		Classifier classifier = this.get_classifier_by_given_name(given_name);

		this.classifier_map.remove(given_name);
		this.classifier_map.put(given_name, Pair.create(classifier, new Date()));

		Running_Reader reader = new Running_Reader(this.process_Definition_Id, process_Instance_Id);
		Data_Set evaluation_set = reader.get_instance_flows(1);
		if(evaluation_set.data_set.size() > 1) {
			this.log_error("Process Instance ID '" + process_Instance_Id + "' is not unique. Multiple reads occured!", Communication_Exception.class);
		}
		return classifier.evaluate(evaluation_set);
	}

	@Override
	public Metric train(String given_name) throws Exception {
		Metric ret = null;
		try {
			Data_Reader dr = new History_Reader(this.process_Definition_Id);
			Data_Set data_set = dr.get_instance_flows();
			List<Data_Set> data = data_set.split(0.8, 0.2);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e) {
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}

	@Override
	public Metric train(String given_name, double ratio) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			Data_Reader dr = new History_Reader(this.process_Definition_Id);
			Data_Set data_set = dr.get_instance_flows();
			List<Data_Set> data = data_set.split(ratio, 1 - ratio);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e) {
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}

	@Override
	public Metric train(String given_name, Date start_date, Date finished_date) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			Data_Reader dr = new History_Reader(this.process_Definition_Id, start_date, finished_date);
			Data_Set data_set = dr.get_instance_flows();
			List<Data_Set> data = data_set.split(0.8, 0.2);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e) {
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	@Override
	public Metric train(String given_name, double ratio, Date start_date, Date finished_date) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			Data_Reader dr = new History_Reader(this.process_Definition_Id, start_date, finished_date);
			Data_Set data_set = dr.get_instance_flows();
			List<Data_Set> data = data_set.split(ratio, 1 - ratio);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e) {
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	@Override
	public Metric train(String given_name, Data_Set data_set, double training_ratio, double evaluation_ratio) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			List<Data_Set> data = data_set.split(training_ratio, evaluation_ratio);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e){
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	@Override
	public Metric train(String given_name, String data_set_path, double training_ratio, double evaluation_ratio) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			Data_Set data_set = this.load_Data_Set(data_set_path);
			List<Data_Set> data = data_set.split(training_ratio, evaluation_ratio);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e){
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	@Override
	public Metric train(String given_name, String training_set_path, String evaluation_set_path) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			Data_Set data_set_01 = this.load_Data_Set(training_set_path);
			Data_Set data_set_02 = this.load_Data_Set(evaluation_set_path);
			ret = this.train(given_name, data_set_01, data_set_02);
		} catch (Exception e){
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	@Override
	public Metric train(String given_name, InputStream data_stream) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			List<Data_Set> data = Data_Set.input_stream2Data_Set(data_stream, this.process_Definition_Id).split(0.8, 0.2);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch (Exception e){
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	@Override
	public Metric train(String given_name, InputStream data_stream, double training_ratio, double evaluation_ratio) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			List<Data_Set> data = Data_Set.input_stream2Data_Set(data_stream, this.process_Definition_Id).split(training_ratio, evaluation_ratio);
			ret = this.train(given_name, data.get(0), data.get(1));
		} catch(Exception e){
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	
	@Override
	public Metric train(String given_name, InputStream training_stream, InputStream evaluation_stream) throws Exception {
		this.set_training_status(given_name, false, false, 0);
		Metric ret = null;
		try {
			ret = this.train(given_name, Data_Set.input_stream2Data_Set(training_stream, this.process_Definition_Id), Data_Set.input_stream2Data_Set(evaluation_stream, this.process_Definition_Id));
		} catch(Exception e){
			this.log_error("Retraining for classifier " + given_name + " failed! " + e.getMessage());
			e.printStackTrace();
			this.set_training_status(given_name, true, false,  0);
			throw e;
		}
		return ret;
	}
	
	@Override
	public Metric train(String given_name, Data_Set training_set, Data_Set evaluation_set) throws Exception {
		Metric metric = null;
		this.active_training.add(given_name);
		if(this.untrained_classifier_map.containsKey(given_name)) {
			// first train
			Classifier classifier = this.untrained_classifier_map.get(given_name);
				
			metric = classifier.train(training_set, evaluation_set);
				
			classifier.get_Meta_Data().setVersion(1);
			this.untrained_classifier_map.remove(given_name);
			this.classifier_map.put(given_name, Pair.create(classifier, new Date()));
				
			classifier.get_Meta_Data().set_metric(metric);
			classifier.last_calculated_metric = (classifier.last_calculated_metric == null) ? metric : classifier.last_calculated_metric;
			this.save_Classifier(classifier, Folder.Trained);
			this.log_message("Initial training for classifier " + given_name + " successfull! New version is " + classifier.get_version() + ".");
			this.set_training_status(given_name, true, true, 0);
		}else {
			metric = this.retrain_classifier(given_name, training_set, evaluation_set);
		}
		this.active_training.remove(given_name);
		return metric;
	}

	public void stop_training(String given_name) throws Exception {
		this.get_classifier_by_given_name(given_name).stop_training();
		this.active_training.remove(given_name);
	}
	
	public void revert_classifier(String given_name) throws Exception {
		this.revert_classifier(given_name, this.get_classifier_by_given_name(given_name).get_version() - 1);
	}
	
	public void revert_classifier(String given_name, Integer to_version) throws Exception {
		Classifier classifier 		= this.get_classifier_by_given_name(given_name);
		Classifier rev_classifier = null;
		int pre_version = 0;
		if(classifier.get_version() > 1) {
			while(classifier.get_version() > to_version) {
				pre_version 	= classifier.get_version() - 1;
				String rev_path = this.get_classifier_path(given_name, Folder.Historic, Integer.toString(pre_version));
				rev_classifier 	= this.deserialize_Classifier(new File(rev_path));
				
				String current_version = Integer.toString(classifier.get_version());
				// delete classifier
				this.delete_file(new File(this.get_classifier_path(given_name, current_version)));
				// delete subfiles
				for(File f : ((classifier.get_subfiles() == null) ? new ArrayList<File>() : classifier.get_subfiles())) {
					this.delete_file(f.getAbsoluteFile());
				}
				// delete meta
				this.delete_file(new File(this.get_meta_path(given_name, current_version)));

				// move historic to trained
				
				if(this.classifier_map.containsKey(given_name)) {
					this.classifier_map.remove(given_name);
				}
				this.log_message("Reverted " + given_name + " from version " + classifier.get_version() + " to "+ pre_version + ".");
				classifier = rev_classifier;
			}
			this.move(rev_classifier, Folder.Historic, Folder.Trained, pre_version);
		}else {
			this.log_error("There is no point to revert " + given_name + " to, because the version is < 2.", Classifier_Exception.class);
		}
	}




	private Data_Set load_Data_Set(String path) throws Exception {
		Data_Set data_set = null;
		if(path.toLowerCase().endsWith(".xes")) {
			data_set = Data_Set.import_XES(path, null);
		}else if(!path.toLowerCase().endsWith(".ser")){
			data_set = Data_Set.import_SER(path);
		}else {
			this.log_error("The given path does not end with '.xes' nor '.ser'!", Data_Exception.class);
		}
		return data_set;
	}

	@Override
	public ClassifierMeta get_meta_data(String given_name) throws Exception {
		Classifier classifer;
		if(this.default_classifier_given_name != null && this.default_classifier_given_name.equals(given_name)) {
			classifer = this.default_classifier;
		}else if(this.classifier_map.containsKey(given_name)) {
			classifer = this.classifier_map.get(given_name).getFirst();
		}else if(this.untrained_classifier_map.containsKey(given_name)) {
			classifer = this.untrained_classifier_map.get(given_name);
		}else {
			String path = this.get_classifier_path(given_name);
			if(path == null) {
				this.log_error("The requested meta data for classifier " + given_name + " do not exist.", Communication_Exception.class);
			}
			return this.deserialize_meta_data(new File(path));
		}
		return classifer.get_Meta_Data();
	}

	@Override
	public void set_meta_data(String given_name, ClassifierMeta meta_data) throws Exception {
		this.get_classifier_by_given_name(given_name).set_Meta_Data(meta_data);
	}

	@Override
	public List<PredictionType> get_prediction_type(String givenName, boolean is_given_name) throws Exception {
		if (is_given_name) {
			return this.get_classifier_by_given_name(givenName).get_prediction_type();
		}
		else {
			return ReflectionReads.getInstanceByName(givenName).get_prediction_type();
		}
	}

	@Override
	public void merge_Classifier(String new_given_name, String author, String... given_names) throws Exception {
		List<String> classifiers_str = Arrays.asList(given_names);
		this.merge_Classifier(new_given_name, author, classifiers_str);
	}

	private Metric retrain_classifier(String given_name, Data_Set training_set, Data_Set evaluation_set) throws Exception {
		Classifier classifier = this.get_classifier_by_given_name(given_name);
		Classifier old_classifier = classifier.clone();
		this.classifier_map.remove(given_name);
		this.classifier_map.put(given_name, Pair.create(classifier, new Date()));

		Metric metric = classifier.train(training_set, evaluation_set);

		if(this.default_classifier == classifier) {
			this.move(old_classifier, Folder.Default, Folder.Historic);
		} else {
			this.move(old_classifier, Folder.Trained, Folder.Historic);
		}
		classifier.change_version(classifier.get_version() + 1);
		classifier.change_lastModified(new Date());
			
		classifier.get_Meta_Data().set_metric(metric);
		this.save_Classifier(classifier, Folder.Trained);
			
		this.log_message("Retraining for classifier " + given_name + " successfull! New version is " + classifier.get_version() + ".");
		this.set_training_status(given_name, true, true, 0);
		return metric;
	}
	@Override
	public void rename_classifier(String given_name, String new_name) throws Exception {
		Classifier classifier = this.get_classifier_by_given_name(given_name);
		Folder folder = null;
		int version = classifier.get_version();
		
		while(version > 0) {			
			classifier = this.get_classifier_by_gn_and_v(given_name, String.valueOf(version));
			folder = this.get_Folder(classifier);
			ClassifierMeta new_meta = new ClassifierMeta(
					new_name,
					classifier.get_version(),
					classifier.get_prediction_type(),
					new Date(),
					new Date(),
					classifier.get_Meta_Data().getAuthor(),
					classifier.get_Meta_Data().getIs_publicly_available(),
					classifier
					);
			classifier.set_Meta_Data(new_meta);
			this.save_Classifier(classifier, folder);

			version--;
		}
		this.classifier_map.remove(given_name);
		this.delete_classifier(given_name);
		this.classifier_map.remove(new_name);
		this.log_message("Classifier " + given_name + " was renamed to " + new_name + ".");
	}
	@Override
	public void copy_classifier(String given_name, String new_name) throws Exception {
		Classifier classifier = this.get_classifier_by_given_name(given_name);
		int version = classifier.get_version();
		while(version > 0) {
			classifier = this.get_classifier_by_gn_and_v(given_name, String.valueOf(version));
			this.copy_classifier(given_name, String.valueOf(version), new_name, this.get_Folder(classifier));
			version--;
		}
		this.classifier_map.remove(given_name);
		this.classifier_map.remove(new_name);
		this.log_message("Classifier " + given_name + " copied and saved as " + new_name + " .");
	}
	private void copy_classifier(String given_name_01, String version_01, String given_name_02, Folder folder) throws Exception {
		Classifier classifier = this.get_classifier_by_gn_and_v(given_name_01, version_01);
		Classifier new_classifier = classifier.clone();
		
		folder = (folder == null) ? this.get_Folder(classifier) : folder;

		ClassifierMeta classifier_meta = new ClassifierMeta(
				given_name_02,
				classifier.get_version(),
				classifier.get_prediction_type(),
				new Date(),
				new Date(),
				classifier.get_Meta_Data().getAuthor(),
				classifier.get_Meta_Data().getIs_publicly_available(),
				new_classifier
				);

		
		new_classifier.set_Meta_Data(classifier_meta);
		
		this.save_Classifier(new_classifier, folder);
		
	}
	
	
	
	private void log_message(String message) {
		this.logger.info("["+ this.process_Definition_Id +"] " + message);
	}
	private void log_error(String error, Class<? extends Exception> e) throws Exception{
		error = "["+ this.process_Definition_Id +"] " + error;
		this.logger.error(error);
		throw e.getDeclaredConstructor(String.class).newInstance(error);
	}
	private void log_error(String error) throws Exception{
		error = "["+ this.process_Definition_Id +"] " + error;
		this.logger.error(error);
	}
	
	public Metric get_metric(String given_name) throws Exception {
		return this.get_classifier_by_given_name(given_name).get_metric();
	}

	public Future<Boolean> asyc_Request (Runnable request){
		return (Future<Boolean>) this.async_request.submit(request);
	}

	@Override
	public void set_training_status(String given_name, boolean done, boolean success, double accuracy){
		this.training_status_map.put(given_name, new TrainingStatus(done, success, accuracy));
	}

	@Override
	public TrainingStatus get_training_status(String given_name) throws Exception {
		TrainingStatus status = this.training_status_map.get(given_name);
		if (status == null) {
			// data set preparation still in progress
			return new TrainingStatus(false, false, 0);
		}
		
		double acurracy;
		try{
			Metric classifierMetric = this.get_metric(given_name);
			acurracy = classifierMetric.accuracy();
		} catch (Exception e){
			acurracy = 0;
		}
		status.setAccuracy(acurracy);
		if(status.isDone()){
			this.training_status_map.remove(given_name);
			this.active_training.remove(given_name);
		}
		return status;
	}

}
