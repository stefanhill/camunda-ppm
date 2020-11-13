package uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration;

import java.lang.reflect.Method;

import javax.management.InvalidAttributeValueException;

import uni_ko.bpm.Machine_Learning.Metric;

public class CheckMethods {
	public static void checkMethod(Method method) throws InvalidAttributeValueException {
		if(!method.getDeclaringClass().equals(Metric.class)) {
			throw new InvalidAttributeValueException("All used methods have to be declared in the 'Metric' class.");
		}
		if(!Number.class.isAssignableFrom(method.getReturnType())) {
			throw new InvalidAttributeValueException("All used methods are expected to return some kind of numeric value.");
		}
		if(!(method.getParameters().length == 0)) {
			throw new InvalidAttributeValueException("It is not supported to use methods which take paramters.");
		}
		
		
	}
}
