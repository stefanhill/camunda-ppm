package uni_ko.bpm.Machine_Learning.Util;

import java.io.Serializable;

public class Classifier_Short_Description implements Serializable{

	public String given_name;
	public Integer version;
	public Boolean is_publicly_available;
	
	public Classifier_Short_Description(String given_name, Integer version, Boolean is_publicly_available) {
		super();
		this.given_name = given_name;
		this.version = version;
		this.is_publicly_available = is_publicly_available;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this.given_name.equals(((Classifier_Short_Description)obj).given_name)
				&& this.version == ((Classifier_Short_Description)obj).version
		) {
			return true;
		}
		return false;
	}
	
}
