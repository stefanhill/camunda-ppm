package uni_ko.bpm.Machine_Learning;

public class Metric_Exception extends Exception{
	private static final long serialVersionUID = 2823940551919538489L;
	
	public Metric_Exception(){
		   super("Basic Metric Exception");
	}
	public Metric_Exception(String error){
		super(error);
	}
}
