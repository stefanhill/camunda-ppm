package uni_ko.bpm.Machine_Learning.NGram;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;

/**			some useful NGram utilities
 * 
 * @author 	Richard Fechner rfechner@uni-koblenz.de
 *
 */
public class NGramUtilities {

    /**
     * @param separated: A sequence upon which the N-Grams are supposed to
     *                   be generated
     * @param N:         Length of the N-Grams
     * @return: List of all possible N-Grams
     */
    public static List<String> allNGrams(List<String> separated, int N)
            throws IllegalArgumentException {

         int arrlen = separated.size();

        //check for N > sequence word length!

        if (N > arrlen || N <= 0)
            throw new IllegalArgumentException("N > sequence word length or N <= 0, no possible N - Grams!");


        List<String> ret = new ArrayList<>();

        for (int i = 0; i < arrlen - N + 1; i++) {
            ret.add(concatStrings(separated, i, i + N));
        }

        return ret;

    }

    /**
     * @param separated: List<String> upon which the N-Gram is supposed
     *                   to be generated
     * @param start:     Start index
     * @param end:       End index
     * @return: A singular N-Gram as a String
     */
    public static String concatStrings(List<String> separated, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append(separated.get(i)).append(";");
        }
        // remove last character ';'
        return sb.substring(0, sb.length()-1);

    }





    /**
     *
     * @param row               HashMap which's values are to be copied into a new list
     * @param lambda            factor to control probability mass
     * @param processIDs_idx    a HashMap to keep track of the right indices,
     *                          without this, we might end up confusing labels with values in the list.
     *
     * @return                  a list of probabilities in order of processIDs_idx
     */
    public static ArrayList<Float> transformAndScale(HashMap<String, Float> row, float lambda, HashMap<String, Integer> processIDs_idx){
        ArrayList<Float> ret = new ArrayList<>();
        for(int i = 0; i < processIDs_idx.size(); i++){
            ret.add((float)0);
        }
        Iterator it = row.entrySet().iterator();
        do{
            HashMap.Entry pair = (HashMap.Entry) it.next();
            ret.set(processIDs_idx.get(pair.getKey().toString()),  (float)pair.getValue()*lambda);
        }while(it.hasNext());

        return ret;

    }

    /**
     *
     * @param list1 list to be merged with list2 into a new list
     * @param list2 list to be merged with list1 into a new list
     *
     * @return      each index of the returned list is the sum of the corresponding indices of the two param lists
     */
    public static List<Float> mergeLists(List<Float> list1, List<Float> list2){
        List<Float> ret = new ArrayList<>();
        for(int i = 0; i < list1.size(); i++)
            ret.add(list1.get(i) + list2.get(i));
        return ret;
    }

    /**
     *
     * @param unigramProbabilities empty HashMap to be filled with raw probabilities
     *                             (occurrences of word x / total word count)
     * @param trainingData data which the probabilities are being calculated from
     */
    public static void calculateUnigramProbabilities(HashMap<String, Float> unigramProbabilities, List<List<String>> trainingData){

        for(List<String> list : trainingData){
            for(String s : list){
                float count = unigramProbabilities.containsKey(s) ? (float)(unigramProbabilities.get(s)) : 0;
                unigramProbabilities.put(s, count+1);
            }
        }

        // calculate unigram probabilities
        Iterator it = unigramProbabilities.entrySet().iterator();
        float totalcount = (float)unigramProbabilities.values().stream().mapToDouble(x -> x).sum();
        while(it.hasNext()){
            HashMap.Entry entry = (HashMap.Entry)it.next();
            entry.setValue((float)entry.getValue()/totalcount);
        }
    }

    public static void calculateProbability(HashMap<String, HashMap<String, Float>> map){
        for(HashMap<String, Float> m : map.values()){
            float lambda = 0;

            for(float f : m.values())
                lambda += f;

            for (HashMap.Entry<String, Float> entry : m.entrySet()) {
                entry.setValue(entry.getValue()/lambda);
            }
        }

    }

    public static void calculateAllProbMaps(HashMap<Integer, HashMap<String, HashMap<String, Float>>> map){
        for(HashMap<String, HashMap<String, Float>> m : map.values())
            calculateProbability(m);
    }




    /**
     *
     * @param N             length of NGram
     * @param trainingData  list of list of activity-chains upon which the HashMap is to be filled
     * @return              a 2D - like structure of HashMaps.
     *                      The count of the successors of each NGram is kept track of
     */
    public static HashMap<String, HashMap<String, Float>> createMap(int N, List<List<String>> trainingData){

        /*
               for different N( == ngram_size), increment the successor of the n-1-gram in the HashMap
               e.g. f(3, [p1, p2, p3, p4, p5], {p1 : 0, p2 : 1 ...}, [[p1, p2, p3], [p1, p2, p4] ... ])
               should yield:
               {"p1;p2":{p1 :0, p2: 1 ...}, "p2;p3" : {p1: 1, p2: 2} ... }
               to cover all possibilities much like in a matrix.
         */

        HashMap<String, HashMap<String, Float>> ret = new HashMap<>();

        for(List<String> list : trainingData){
            int len = list.size();

            //check for N > sequence word length!

            if (N >= len || N <= 0)
                continue;
            //throw new IllegalArgumentException("N > sequence word length or N <= 0, no possible N - Grams!");

            for (int i = 0; i < len - N + 1; i++) {
                String s = NGramUtilities.concatStrings(list, i, i + N - 1);
                String w = list.get(i+N-1);

                if(ret.containsKey(s)){
                    float count = ret.get(s).containsKey(w) ? ret.get(s).get(w) : 0;

                    ret.get(s).put(w, count+1);
                }else{
                    ret.put(s, new HashMap<>());
                    ret.get(s).put(w, (float)1.0);
                }


            }


        }

        return ret;
    }
    
    

    /**
     *
     * @param allMaps           	a collection of all (probability!) HashMaps
     * @param leftoverProb     		the leftover probability mass. This way, we can weigh the different
     *                          	predictions of each NGram HashMap
     * @param history  				list of Strings / processIDs of which the successor is to be predicted
     * @param processIDsIdx    		simple HashMap to keep track of indices
     *
     * @return                  	a list of the summed up log probabilities in order of processIDsIdx
     */
    public static ArrayList<Float> predictNext(HashMap<Integer, HashMap<String ,HashMap<String, Float>>> allMaps,
                                               float leftoverProb,
                                               float lambda,
                                               List<String> history,
                                               HashMap<String, Integer> processIDsIdx,
                                               HashMap<String, Float> unigramProbabilities) {

    	int len = history.size();
    	if(len > allMaps.size()) {
            return predictNext(allMaps, leftoverProb,lambda, history.subList(1, len), processIDsIdx, unigramProbabilities);
    	}
        
        // check for recursive exit condition
        if(len == 0){
            // we still have to respect unigrams!
        	return NGramUtilities.transformAndScale(unigramProbabilities, leftoverProb, processIDsIdx);
        }

        String target = String.join(";", history);


        /*  ask the corresponding HashMap for (len+1)-Grams
            a activityChainHistory of length 1 will ask the HashMap of bigrams
            for the most likely result
        */
        if(allMaps.get(len+1).containsKey(target)){
            float scalar =  leftoverProb * lambda;
            leftoverProb *= (1-lambda);
            HashMap<String, Float> row = allMaps.get(len+1).get(target);

            ArrayList<Float> tmp = NGramUtilities.transformAndScale(row, scalar, processIDsIdx);

            return (ArrayList<Float>) NGramUtilities.mergeLists(tmp, predictNext(allMaps, leftoverProb,lambda, history.subList(1, len), processIDsIdx, unigramProbabilities));


        }
        else
        	return predictNext(allMaps, leftoverProb,lambda, history.subList(1, len), processIDsIdx, unigramProbabilities);


    }

    /**
     *
     * @param N         upper border (inclusive)
     * @param data      list of all process-chains
     * @return          all HashMaps of occurrences of NGrams with size from 2 to N(inclusive)
     */
    public static HashMap<Integer, HashMap<String ,HashMap<String, Float>>> createAllMaps(int N, List<List<String>> data){

        if(N<2) throw new IllegalArgumentException("your chosen ngram-size is too small, you passed: "+N+", \n" +
                                                    "but the minimum ngram-size is 2.");
        HashMap<Integer, HashMap<String, HashMap<String, Float>>> allMaps = new HashMap<>();

        for(int i = 2; i <= N; i++){
            allMaps.put(i, createMap(i, data)); // index 0 --> bigrams, index 1 --> trigrams...
        }
        return allMaps;
    }
    
   

    /**
     *
     * @param ds_list   list which is supposed to be cutoff at index @param cutoff
     * @param cutoff    [1,2,3] with cutoff 1 will yield [1,2]
     *                  [1,2,3,4] with cutoff 3 will yield [1] ...
     * @return          a List of cutoff lists
     */
    public static List<List<String>> nGramTestList(List<List<String>> ds_list, int cutoff){
        if(cutoff > ds_list.size()-2)
            throw new IllegalArgumentException("parameter cutoff too big! we expected a cutoff < "+(ds_list.size()-2)+", but got: "+cutoff);

    	List<List<String>> ret = new ArrayList<>();
    	
    	for(List<String> list : ds_list)
    		ret.add(list.subList(0, list.size()-cutoff));
    	return ret;
    }

    /**
     *
     * @param ds_list   List of lists in which to locate the target
     * @param cutoff    [1,2,3] with cutoff 1 will yield 3
     *                  [1,2,3,4] with cutoff 3 will yield 2
     * @return          list of targets
     */
    public static List<String> nGramTargetList(List<List<String>> ds_list, int cutoff){
        if(cutoff > ds_list.size()-2)
            throw new IllegalArgumentException("parameter cutoff too big! we expected a cutoff < "+(ds_list.size()-2)+", but got: "+cutoff);

        List<String> ret = new ArrayList<String>();
    	
    	for(List<String> list : ds_list)
    		ret.add(list.get(list.size()-cutoff));
    	return ret;
    }

    /**
     *
     * @param activityChains    list of (shortened) activity-chains
     * @param targetList        list of target strings
     * @param lambda            factor by which longer activity chains are weighted in comparison to
     *                          shorter chains
     * @param uniqueTaskName    HashMap of unique names of every Task in the training set
     * @param probs             probability HashMap of Classifier
     * @param unigrams          unigram HashMap of Classifier
     * @return                  [hits, misses]
     */
    public static List<Integer> getHitsAndMisses(List<List<String>> activityChains,
    											 List<String> targetList,
    											 float lambda,
    											 HashMap<String, Integer> uniqueTaskName,
    											 HashMap<Integer, HashMap<String, HashMap<String, Float>>> probs,
    											 HashMap<String, Float> unigrams){
    	
    	
    	
    	// create counter to track the true/false prediction ratio
		int true_positive_counter = 0;
		for(int i = 0; i < activityChains.size(); i++) {
			// if the predicted String == target.get(i) --> counter ++

			//find max index in the values returned by NGramUtilities.predictNext()
			List<Float> values = NGramUtilities.predictNext(probs, 1,lambda, activityChains.get(i), uniqueTaskName, unigrams);
			
			int maxIndex = 0;
			float maxValue = values.get(0);
			String curTarget = "";
			
			for(int j = 0; j < values.size(); j++) { 
				float curVal = values.get(j);
				if(curVal > maxValue) {
					maxIndex = j; 
					maxValue = curVal;
				}		
			}
			//iterate through the HashMap, get the key corresponding to value maxIndex
            for (Map.Entry<String, Integer> entry : uniqueTaskName.entrySet()) {
                if (entry.getValue().equals(maxIndex)) {
                    curTarget = entry.getKey();
                    break;
                }
            }
            
			if(curTarget.equals(targetList.get(i))) // we have to use equals here, otherwise bad shit happens
				true_positive_counter ++;
			
		}
		
        return Arrays.asList(true_positive_counter, activityChains.size()-true_positive_counter);
    }

    /**
     *
     * @param keys HashMap from which we only need the keys to be zipped together
     * @param values List of floats which are to be zipped with the keys
     * @return a HashMap of Key-Value-Pairs
     */
    public static HashMap<String, Float> zipKeysAndValues(HashMap<String, Integer> keys, List<Float> values){
    	HashMap<String, Float> ret = new HashMap<>();

        for (String cur : keys.keySet()) {
            ret.put(cur, values.get(keys.get(cur)));
        }
    	return ret;
    }

}
