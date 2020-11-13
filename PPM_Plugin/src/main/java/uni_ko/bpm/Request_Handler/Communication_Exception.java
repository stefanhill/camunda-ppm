package uni_ko.bpm.Request_Handler;

public class Communication_Exception extends Exception{
	private static final long serialVersionUID = 2893588519038184035L;
	public Communication_Exception(){
		   super("Basic Data Exception");
	}
	public Communication_Exception(String error){
		super(error);
	}
}
