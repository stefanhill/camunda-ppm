package uni_ko.JHPOFramework.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nd4j.shade.guava.collect.ImmutableSet;
import org.nd4j.shade.guava.collect.Sets;
import org.nd4j.shade.guava.reflect.ClassPath;
import org.nd4j.shade.guava.reflect.ClassPath.ClassInfo;

import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;

public class ReflectionReads {

	public static List<Class<? extends OptimizationAlgorithm>> getPossibleOptimizationClasses() throws Exception{
	    ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
	        
	    ImmutableSet<ClassInfo> i = classpath.getTopLevelClassesRecursive("uni_ko.JHPOFramework.SimulationEnvironment.Optimizer");
	    return ReflectionReads.getPossibleOptimizationClasses(Sets.newHashSet(i), OptimizationAlgorithm.class);

	}
	private static List<Class<? extends OptimizationAlgorithm>> getPossibleOptimizationClasses(Set<ClassInfo> set, Class this_class) throws Exception{
		List<Class<? extends OptimizationAlgorithm>> buffer = new ArrayList<Class<? extends OptimizationAlgorithm>>();
		for(ClassInfo i : set) {
			Class abst_class = i.load();
			if(abst_class != null && !abst_class.equals(this_class) && this_class.isAssignableFrom(abst_class)) {
				if(!Modifier.isAbstract(abst_class.getModifiers())) {
					buffer.add(abst_class);
				}else {
					ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
					buffer.addAll(
							ReflectionReads.getPossibleOptimizationClasses(classpath.getTopLevelClassesRecursive(i.getPackageName()), abst_class)
							);
				}
			}
		}
		return buffer;
	}
	
}
