package uni_ko.bpm.Machine_Learning.NGram;

import java.util.*;
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

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.*;
import uni_ko.bpm.Machine_Learning.MetricImpl.UnaryMetric;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;

import static java.util.stream.Collectors.toList;

/**			Implentation of an NGramClassifier
 * 			
 * 			The activity history of a process instance can be treated as 
 * 			a chain of words, the words being the unique activityId of each activity.
 * 			Upon this, we can construct a statistical method
 * 			for predicting the next activity through a hybrid between
 * 			katz backoff and linear interpolation.
 * 
 * 			if you are interested in reading about this, i can recommend
 * 			this pdf: https://web.stanford.edu/~jurafsky/slp3/3.pdf
 * 			under chapter 3 you can find very useful information regarding
 * 			linear interpolation and backoff.			
 * 
 * 
 * @author 	Richard Fechner rfechner@uni-koblenz.de
 *
 */
public class NGramClassifier extends Classifier {

	private static final long serialVersionUID = 6304577663519041034L;
	private HashMap<Integer, HashMap<String, HashMap<String, Float>>> probs = new HashMap<>();
    private HashMap<String, Float> unigrams = new HashMap<>();
    private HashMap<String, Integer> uniqueTaskName;
    private int ngramSize;
    private float optimalLambda = 0.75f;
    

    public NGramClassifier() {
        super();
    }

    /**
     * @param trainingSet 	Data set for training
     * @param testSet     	Data set for testing
     * @return 				a Metric containing true positives and false positives
     * @throws Metric_Exception
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    @Override
    public Metric train(Data_Set trainingSet, Data_Set testSet) throws Exception {
        boolean debug = false;
    	uniqueTaskName = trainingSet.get_unique_Task_Name();

        // convert Data_set trainingSet and testSet to List<list<string>>
        List<List<String>> training_list = trainingSet.<String>get_set_parameter("concept:name");
        List<List<String>> test_list = testSet.<String>get_set_parameter("concept:name");

        
        // create HashMap from (ngram_size)-grams to bigrams with occurrences of
        // different n-1 - grams and their n-th word(successor)
        probs = NGramUtilities.createAllMaps(this.ngramSize, training_list);

        // calculate Probabilities
        NGramUtilities.calculateAllProbMaps(probs);

        //calculate unigram probabilities
        NGramUtilities.calculateUnigramProbabilities(unigrams, training_list);

        // we have to shorten the lists by 1 and use the cutoff as target for our prediction
        List<List<String>> shortenedTestList = NGramUtilities.nGramTestList(test_list, 1);
        List<String> targetList = NGramUtilities.nGramTargetList(test_list, 1);
        
        
        // now, try to predict the next activity of the shortened list --> hopefully we hit the target!
        List<Integer> accuracy = NGramUtilities.getHitsAndMisses(shortenedTestList, targetList,this.optimalLambda, uniqueTaskName, probs, this.unigrams);
        double hits = (double) accuracy.get(0);
        double misses = (double) accuracy.get(1);

        Metric ret = new UnaryMetric(hits, misses, this.ngramSize);
        this.last_calculated_metric = ret;
        return ret;

    }

    /**
     *
     * @param 	dataSet Data_Set containing task-chains which are to be fed into the predict method
     * @return 	a List of Classifications, each corresponding with a flow from the @param dataSet
     * @throws 	ClassificationException the method evaluate uses the pre - calculated HashMap to return its values.
     *                                  if the HashMap has not been initialized, you should call the train method first!
     * @throws 	IllegalArgumentException
     * @throws 	IllegalAccessException
     * @throws 	NoSuchFieldException
     * @throws 	SecurityException
     */
    @Override
    public List<Classification> evaluate(Data_Set dataSet) throws Exception {
        if (this.probs == null)
            throw new ClassificationException("counldn't find probability HashMap, maybe train the model first!");
        
        //transform Data_Set into list, so that NGramUtilities can work with it
        List<List<String>> data_list = dataSet.<String>get_set_parameter("concept:name");
        List<Classification> ret = new ArrayList<>();

        // for each instance history we have to predict
        for (List<String> list : data_list) {
            
        	// extract the probability for each activity to succeed the current history
        	List<Float> values = NGramUtilities.predictNext(this.probs, 1,this.optimalLambda, list, this.uniqueTaskName, this.unigrams);
            
            // make the probabilities pretty, pass them to the Classification Object
            HashMap<String, Float> evals = NGramUtilities.zipKeysAndValues(this.uniqueTaskName, values);
            ret.add(new Classification(evals, PredictionType.ActivityPrediction));
        }
        return ret;
    }

    public HashMap<Integer, HashMap<String, HashMap<String, Float>>> getProbs() {
        return probs;
    }

    public void setProbs(HashMap<Integer, HashMap<String, HashMap<String, Float>>> probs) {
        this.probs = probs;
    }


    public HashMap<String, Float> getUnigrams() {
        return unigrams;
    }

    public void setUnigrams(HashMap<String, Float> unigrams) {
        this.unigrams = unigrams;
    }

    public int getNgramSize() {
        return ngramSize;
    }

    public void setNgramSize(int ngramSize) {
        this.ngramSize = ngramSize;
    }


    public HashMap<String, Integer> getUniqueTaskName() {
        return uniqueTaskName;
    }

	@Override
	public List<Parameter_Communication_Wrapper> configurational_parameters() {
		List<Parameter_Communication_Wrapper> parameters= new ArrayList<Parameter_Communication_Wrapper>();
		parameters.add(new Parameter_Communication_Wrapper_Lists<PredictionType>(
				0, 
				"Prediction-Types", 
				"Defines which types this classifier will predict.", 
				false,
				this.prediction_types,
				PredictionType.class, 
				Arrays.asList(PredictionType.ActivityPrediction), 
				true
				)
		);
		parameters.add(new Parameter_Communication_Wrapper_Single<Integer>(
							1, 
							"N-Gram size", 
							"Infortext for N-Gram size", 
							false,
							this.ngramSize,
							Integer.class, 
							5)	// suggested value is 5
				);
		return parameters;
	}

	@Override
	public void set_configurational_parameters(HashMap<Integer, Object> parameters) {
        try {
            this.prediction_types = ((List<String>) parameters.get(0)).stream()
                    .map(PredictionType::valueOf)
                    .collect(toList());
        } catch (Exception e) {
            this.prediction_types = ((List<PredictionType>) parameters.get(0));
        }
		this.ngramSize = (int) parameters.get(1);
	}


    public void setOptimalLambda(float lambda){
	    this.optimalLambda = optimalLambda;
    }
	
	@Override
	public List<PredictionType> get_prediction_type() {
		List<PredictionType> prediction_types = new ArrayList<>();
		prediction_types.add(PredictionType.ActivityPrediction);
		return prediction_types;
	}
}
