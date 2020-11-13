package uni_ko.bpm.Tests;


import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import uni_ko.bpm.Data_Management.*;
import uni_ko.bpm.Machine_Learning.MetricImpl.UnaryMetric;
import uni_ko.bpm.Machine_Learning.NGram.NGramClassifier;

public class NGramTimeTesting {
    /*
    simple main method to test the functionality of the NGram classification and prediction.
    method flow:
    get xes file -> create Data_Set from xes file -> split Data_Set into trainingSet and testingSet ->
    create Classifier Object -> print true pos, false pos of Metric to console
     */
    /*public static void main(String[] args) throws Exception {
        //Data_Set ds = Data_Set.import_XES("C:\\#uni\\summer20\\prprak\\camunda-ppm\\PPM_Plugin\\Dev_Data\\10k_test.xes");
        Data_Set ds = Data_Set.import_XES("C:\\#uni\\summer20\\prprak\\camunda-ppm\\PPM_Plugin\\src\\test\\resources\\test01.xes");
        NGramTime c = new NGramTime();

        HashMap<Integer, Object> init_values = new HashMap<>();
        init_values.put(0, 10);
        c.set_configurational_parameters(init_values);

        List<Data_Set> dataSets = ds.split(0.8, 0.2);

        UnaryMetric m;
        try {
            m = (UnaryMetric) c.train(dataSets.get(0), dataSets.get(1));
            System.out.println("true pos: " + m.getTrueClassifications() + " false pos: " + m.getFalseClassifications());
            System.out.println("Accuracy: " + m.calculateAccuracy());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        c.setOptimalLambda(0.9999f);

        boolean debug = true;
        //c.printTestingDataFor(dataSets.get(1), debug);

    }*/
}



