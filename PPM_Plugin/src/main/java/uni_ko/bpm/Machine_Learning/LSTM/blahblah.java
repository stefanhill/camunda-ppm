package uni_ko.bpm.Machine_Learning.LSTM;

import uni_ko.bpm.Machine_Learning.Metric;

@Deprecated
public class blahblah {

	public static void main(String[] args) throws Exception {
		// also copy subfolder ser
		
		
		blahblah b = new blahblah();
		
		Thread training_thread = new Thread("New Thread") {
			      public void run(){
			    	  	try {
							//b.do_training();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			      }
		 };
		 training_thread.start();
		 
		 Thread t1 = new Thread("New Thread") {
		      public void run(){
		    	  try {
					Thread.sleep(20000);
		    	  	while(training_thread.isAlive()) {
		    	  		b.show_stuff();
		    	  	}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		      }
		 };
	 
		 // t1.start();
		
	}
	
	public LSTM_Classifier l;
	/*public void do_training() throws Exception {
		// Hambrücken
		// Data_Set ds = Data_Set.import_XES("C:\\Users\\Nico\\git\\camunda-ppm\\PPM_Plugin\\Dev_Data\\test01.xes");
		// Notebook
		Data_Set ds = Data_Set.import_XES("C:\\Users\\nbart\\git\\camunda-ppm\\PPM_Plugin\\Dev_Data\\test01.xes");
		//Data_Set ds = Data_Set.import_XES("C:\\Users\\nbart\\git\\camunda-ppm\\PPM_Plugin\\Dev_Data\\10k_test.xes");
		// ds = (ds.split(0.25, 0.0)).get(0);

		System.out.println("Shortest Path:"+ds.get_Shortest_Path()+"          Longest Path:"+ds.get_Longest_Path());
		int l_path = ds.get_Longest_Path();
		int s_path = ds.get_Shortest_Path();
		System.out.println("Length: "+ds.get_unique_Task_Name().size());
		
		// medium expected length in real world applications
		int trainings_chain_length = ((int)((l_path-s_path)/2))+s_path;
		
		Data_Reader reader = new Token_Reader(ds, trainings_chain_length, trainings_chain_length);
		
        Data_Set tokenized_ds = reader.get_instance_flows();
        
        List<Data_Set> ds_l = tokenized_ds.split(0.75, 0.25);
        
        System.out.println("size 0:"+ds_l.get(0).get_flow_count() + "     size 1: "+ +ds_l.get(1).get_flow_count());
        
		this.l = new LSTM_Classifier();
		
		HashMap<Integer, Object> parameters = new HashMap<>();
		
		List<PredictionType> prediction_types = new ArrayList<>();
		prediction_types.add(PredictionType.ActivityPrediction);
		prediction_types.add(PredictionType.TimePrediction);
//		prediction_types.add(PredictionType.RiskPrediction);
		
		parameters.put(0, prediction_types);
		parameters.put(1, 100);
		parameters.put(2, 5);
		
		parameters.put(3, 3);
		parameters.put(4, 1);
		
		// More than 256 burns my fucking CPU - i want GPU :( so sad - big mad 
		parameters.put(5, 256);
		parameters.put(6, 1000);
		parameters.put(7, 25);

		parameters.put(8, 1e-3);
		parameters.put(9, 0.9);
		parameters.put(10, 0.999);
		parameters.put(11, 1e-8);
		
		l.set_configurational_parameters(parameters);

		Metric m = l.train(ds_l.get(0), ds_l.get(1));

		List<Classification> classifications =  l.evaluate(ds_l.get(1));
		
		
		System.out.println(classifications.get(0).evals.toString());
		

		String path = "C:\\Users\\nbart\\Documents\\testnet-irr2.ser";
		l.exportSER(path);	
				
		System.out.println("SWICTH");
		
		LSTM_Classifier l1 = new LSTM_Classifier();
		l1 =  l1.<LSTM_Classifier>importSER(path);
		
		List<Classification> c = l1.evaluate(ds_l.get(1));
		SamplingMultiDataSetIterator smdsi = new SamplingMultiDataSetIterator(ds_l.get(1), l1.fields, 1);
		MultiDataSet mds = smdsi.sample(1);
		
		System.out.println("Should:" + mds.getLabels()[0]);
		System.out.println(c.get(0).evals.toString());
		System.out.println("Should:" + mds.getLabels()[1]);
		System.out.println(c.get(100).evals.toString());
	//	System.out.println(l1.evaluate(ds_l.get(1)).get(101).evals.toString());
		
	}*/
	
	public void show_stuff() throws InterruptedException {
		try {
			System.out.println("Epochs: "+ this.l.network.getEpochCount());
			Metric m = this.l.get_metric();
			System.out.println("Accuracy: "+ m.accuracy());
		}catch (Exception e) {
			// TODO: handle exception
		}
		Thread.sleep(5000);
	}
}
