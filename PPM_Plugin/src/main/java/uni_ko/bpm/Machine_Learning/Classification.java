package uni_ko.bpm.Machine_Learning;


import java.util.HashMap;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;


public class Classification {
	/*
	 * this will return Data of a Classifier in a currently unknown format....
	 */
	
	public HashMap<String, Float> evals;
	public PredictionType type;
	
	
	public Classification(HashMap<String, Float> evals, PredictionType type) {
		super();
		this.evals = evals;
		this.type = type;
	}
	
	public Classification(float[] output, List<String> node_names, PredictionType type) {
		super();
		this.evals = new HashMap<String, Float>();
		if(node_names.size() == output.length) {
			for(int i = 0; i < node_names.size(); i++) {
				evals.put(node_names.get(i), output[i]);
			}
		}else if(output.length == 1) {
			// Single Classification like Time/Risk
			evals.put("0.0", output[0]);
		}
		this.type = type;
	}


}
