package uni_ko.bpm.Data_Management.XESHandling;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.regex.Pattern;

import uni_ko.bpm.Data_Management.Util.TimeParser;
import weka.core.UnsupportedClassTypeException;

public enum SupportedXESTypes {
	// sorted from specific to unspesific
	EDouble(Pattern.compile("[\\d][.][\\d]+"), Double.class),
	EInteger(Pattern.compile("[\\d]{1,9}"), Integer.class),
	ELong(Pattern.compile("[\\d]+"), Long.class),
	EDate(Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"), Date.class),
	EString(Pattern.compile(".*"), String.class)
	;
	public final Pattern regEx;
	public final Class<?> type;
	
	SupportedXESTypes(Pattern regEx, Class<?> type) {
		 this.regEx = regEx;
		 this.type = type;
    }
	
	public Method getCaster() throws NoSuchMethodException, SecurityException {
		switch (this) {
		case EDouble:
			return Double.class.getMethod("valueOf", String.class);
		case ELong:
			return Long.class.getMethod("parseLong", String.class);
		case EInteger:
			return Integer.class.getMethod("valueOf", String.class);
		case EDate:
			return TimeParser.class.getMethod("createTimestamp", String.class);
		case EString:
			return String.class.getMethod("valueOf", Object.class);
		default:
			return null;
		}
	}
	public static SupportedXESTypes lookup(String s) {
		switch (s) {
		case "double":
			return EDouble;
		case "long":
			return ELong;
		case "int":
		case "integer":
			return EInteger;
		case "date":
			return EDate;
		case "string":
			return EString;
		default:
			return EString;
		}
	}
	public static SupportedXESTypes lookup(Class<?> c) throws UnsupportedClassTypeException {
		for(SupportedXESTypes sXEST : SupportedXESTypes.values()) {
			if(sXEST.type.equals(c)) {
				return sXEST;
			}
		}
		throw new UnsupportedClassTypeException("The requested class type is not supported. Please extends the 'SupportedXESTypes'-enum.");
	}
	
}
