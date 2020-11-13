package uni_ko.bpm.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nd4j.shade.guava.collect.ImmutableSet;
import org.nd4j.shade.guava.collect.Sets;
import org.nd4j.shade.guava.reflect.ClassPath;
import org.nd4j.shade.guava.reflect.ClassPath.ClassInfo;

import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.CheckMethods;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.ClassifierMeta;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Request_Handler.Frontend_Communication_Data;

public class ReflectionReads {
	public static List<String> getPossibleClassifier() throws Exception{
		if(Frontend_Communication_Data.possible_classifier == null) {
			List<String> ret = new ArrayList<String>();
			List<Class<? extends Classifier>> lC = ReflectionReads.getPossibleClassifierClasses();
			for(Class<? extends Classifier> c : lC) {
				ret.add(c.getSimpleName());
			}
			return ret;
		}
		return Frontend_Communication_Data.possible_classifier;
	}


	public static List<Class<? extends Classifier>> getPossibleClassifierClasses() throws Exception{
		if(Frontend_Communication_Data.possible_classifier_classes == null) {
			Frontend_Communication_Data.possible_classifier_classes = new ArrayList<Class<? extends Classifier>>();
	        ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
	        
	        ImmutableSet<ClassInfo> i = classpath.getTopLevelClassesRecursive("uni_ko.bpm.Machine_Learning");
	        List<Class<? extends Classifier>> buffer = ReflectionReads.get_possible_classifier_classes(Sets.newHashSet(i), Classifier.class);
	        
	        Frontend_Communication_Data.possible_classifier_classes = buffer.stream()
	        														.distinct()
	        														.collect(Collectors.toList());
		}
		return Frontend_Communication_Data.possible_classifier_classes;
	}
	private static List<Class<? extends Classifier>> get_possible_classifier_classes(Set<ClassInfo> set, Class this_class) throws Exception{
		List<Class<? extends Classifier>> buffer = new ArrayList<Class<? extends Classifier>>();
		for(ClassInfo i : set) {
			Class abst_class = i.load();
			if(abst_class != null && !abst_class.equals(this_class) && this_class.isAssignableFrom(abst_class)) {
				if(!Modifier.isAbstract(abst_class.getModifiers())) {
					buffer.add(abst_class);
				}else {
					ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
					buffer.addAll(
							ReflectionReads.get_possible_classifier_classes(classpath.getTopLevelClassesRecursive(i.getPackageName()), abst_class)
							);
				}
			}
		}
		return buffer;
	}

	
	public static Classifier deserializeClassifier(File classifier_file, String classifierName) throws Exception {
		// this.get_classifier_name(classifier_file)
		Classifier classifier = ReflectionReads.getInstanceByName(classifierName);
		classifier = classifier.importSER(classifier_file.getAbsolutePath());

		return classifier;
	}
	public static Classifier getInstanceByName(String classifier_name) throws Exception {
        ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
		ImmutableSet<ClassInfo> i = classpath.getTopLevelClassesRecursive("uni_ko.bpm.Machine_Learning");
		List<Class<?>> cs = i.stream()
        	.filter(c -> c.getSimpleName().equals(classifier_name))
        	.map(c -> c.load())
        	.collect(Collectors.toList());
		System.out.println("SAVE: " + classifier_name + " <- " + cs.size() + ":" + i.size());
		return (Classifier) cs.get(0).getDeclaredConstructor().newInstance();
	}
	
	
	public static List<Method> getMetricMethods() throws Exception{
		List<Method> availableMethods = new ArrayList<Method>();
		for(Method method : Metric.class.getMethods()) {
			try {
				CheckMethods.checkMethod(method);
				availableMethods.add(method);
			}catch (Exception e) {}
		}
		return availableMethods;
	}
	public static List<String> getMetricMethodsAsString() throws Exception{
		List<String> availableMethods = new ArrayList<String>();
		for(Method method : Metric.class.getMethods()) {
			try {
				CheckMethods.checkMethod(method);
				availableMethods.add(method.getName());
			}catch (Exception e) {}
		}
		return availableMethods;
	}
	public static Method getMetricMethod(String name) throws Exception{
		for(Method method : Metric.class.getMethods()) {
			try {
				if(method.getName().equals(name)) {
					CheckMethods.checkMethod(method);
					return method;
				}
			}catch (Exception e) {}
		}
		return null;
	}




}
