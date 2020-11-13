package uni_ko.bpm.Tests;


import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import uni_ko.bpm.Data_Management.*;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.MetricImpl.UnaryMetric;
import uni_ko.bpm.Machine_Learning.NGram.NGramClassifier;

public class NGramTesting {
	/*
	simple main method to test the functionality of the NGram classification and prediction.
	method flow:
	get xes file -> create Data_Set from xes file -> split Data_Set into trainingSet and testingSet ->
	create Classifier Object -> print true pos, false pos of Metric to console
	 */
    public static void main(String[] args) throws Exception {
        //Data_Set ds = Data_Set.import_XES("C:\\Users\\nbart\\git\\camunda-ppm\\PPM_Plugin\\Dev_Data\\10k_test.xes");
        NGramClassifier c = new NGramClassifier();
        Data_Set ds = Data_Set.import_XES("C:\\#uni\\summer20\\prprak\\camunda-ppm\\PPM_Plugin\\Dev_Data\\PermitLog.xes", null);
        
        HashMap<Integer, Object> init_values = new HashMap<>();
        init_values.put(0, Arrays.asList(PredictionType.ActivityPrediction));
        init_values.put(1, 6);
        c.set_configurational_parameters(init_values);
        
        List<Data_Set> dataSets = ds.split(0.8, 0.2);
        
        //c.exportSER("C:\\Users\\nbart\\Documents\\ngram.ser");
        
        UnaryMetric m;
        try {
            m = (UnaryMetric) c.train(dataSets.get(0), dataSets.get(1));
            //System.out.println("true pos: " + m.getTrueClassifications() + " false pos: " + m.getFalseClassifications());
            System.out.println("Accuracy: " + m.accuracy());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        /*
        boolean debug = true;
        //c.printTestingDataFor(dataSets.get(1), debug);
        Data_Set tmp = dataSets.get(0);
        List<List<Double>> l1 = tmp.<Double>get_set_parameter("duration");
        List<Double> l2 = l1.get(0);
        
        for (int i = 0; i < l2.size(); i++) {
			System.out.println(l2.get(i));
		}
        */
        
    }
}



