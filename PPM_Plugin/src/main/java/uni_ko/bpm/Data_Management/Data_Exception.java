package uni_ko.bpm.Data_Management;

public class Data_Exception extends Exception{
	private static final long serialVersionUID = -6478943196599054683L;
	public Data_Exception(){
		   super("Basic Data Exception");
	}
	public Data_Exception(String error){
		super(error);
	}
}
