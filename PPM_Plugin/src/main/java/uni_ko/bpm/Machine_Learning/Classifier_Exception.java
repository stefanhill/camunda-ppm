package uni_ko.bpm.Machine_Learning;

import org.slf4j.Logger;

public class Classifier_Exception extends Exception{

	public Classifier_Exception(){
		   super("Basic Classifier Exception");
	}
	public Classifier_Exception(String error){
		super(error);
	}

}
