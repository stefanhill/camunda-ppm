package uni_ko.bpm.Machine_Learning.LSTM;

import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import uni_ko.JHPOFramework.Structures.Pair;

public class Field_Config {

	public String field_name;
	
	public boolean has_input;
	public boolean one_hot_in;
	
	public boolean has_output;
	public boolean one_hot_out;
	
	public Activation hidden_activation;
	public Activation output_activattion;
	
	public Pair<Integer, Integer> flow_index;

	public LossFunctions.LossFunction loss_function;
	
	public Field_Config(String field_name, boolean has_input, boolean one_hot_in, boolean has_output,
			boolean one_hot_out, Activation hidden_activation, Activation output_activattion,
			Pair<Integer, Integer> flow_index, LossFunctions.LossFunction loss_function) {
		super();
		this.field_name = field_name;
		this.has_input = has_input;
		this.one_hot_in = one_hot_in;
		this.has_output = has_output;
		this.one_hot_out = one_hot_out;
		this.hidden_activation = hidden_activation;
		this.output_activattion = output_activattion;
		this.flow_index = flow_index;
		this.loss_function = loss_function;
	}

 
}
