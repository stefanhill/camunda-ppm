package uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Machine_Learning.Metric;

public class Classifier_Executer implements Callable<Pair<Classifier, Metric>> {
	protected Class<? extends Classifier> classifier;
	protected HashMap<Integer, Object> configuration;
	protected Data_Set training_set;
	protected Data_Set test_set;
	protected int runID;
    public Classifier_Executer(
    		Class<? extends Classifier> classifier, 
    		HashMap<Integer, Object> configuration, 
    		Data_Set training_set,
    		Data_Set test_set,
    		int runID
    		) {
        this.classifier = classifier;
        this.configuration = configuration;
        this.training_set = training_set;
        this.test_set = test_set;
        this.runID = runID;
    }

    @Override
    public Pair<Classifier, Metric> call() throws Exception {
    	Classifier c = this.classifier.getDeclaredConstructor().newInstance();
    	c.set_configurational_parameters(configuration);
    	// Simulate normal creation - Export of Classifier without Meta-Data is not intended
		ClassifierMeta classifier_meta = new ClassifierMeta(
				"TestRun:"+this.runID,
				1,
				c.get_prediction_type(),
				new Date(),
				new Date(),
				"Computer",
				false,
				c
				);
		c.set_Meta_Data(classifier_meta);
		
		
    	return new Pair<>(c, c.train(training_set, test_set));
    }
}