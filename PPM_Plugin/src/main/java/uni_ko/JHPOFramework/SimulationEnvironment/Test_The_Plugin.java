package uni_ko.JHPOFramework.SimulationEnvironment;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.Configuration;
import uni_ko.JHPOFramework.SimulationEnvironment.Data.Data;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.BayesianOptimizer;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.BayesianOptimizer.SupportedClassifier;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.List_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Numeric_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.NGram.NGramClassifier;
import uni_ko.bpm.Machine_Learning.WekaClassifier.RandomForestClassifier;


public class Test_The_Plugin {
	public static void main(String[] args) throws Exception {
		
		String project_name 	= "Test";
		String project_path 	= "C:\\test_plugin\\";
		
		Data data = new Data();
		data.builder()
			.addData(Data_Set.import_XES("C:\\Users\\nbart\\git\\camunda-ppm\\Resources\\fp-camunda\\xes\\DomesticDeclarations.xes", null), 0.05, "Nico01");
		
		
		MetricConfiguration mc = new MetricConfiguration()
										.addMetric("ACC", Metric.class.getMethod("accuracy", null))
										.addMetric("ACC2", Metric.class.getMethod("accuracy", null));
		
		HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations = new HashMap<Class<? extends Classifier>, Pair<List<Integer>,List<Range>>>();
		
		
	
		
		
		// config example for RandomForest - builder pattern
		Configuration c = new Configuration().builder()
										.add_classifier(RandomForestClassifier.class)
		// use all permutations
										.add_parameter(0, new List_Range<PredictionType>(
																0,
																Arrays.asList(
																		PredictionType.ActivityPrediction, 
																		PredictionType.TimePrediction, 
																		PredictionType.RiskPrediction
																		), 
																true,
																PredictionType.class
																)
										)
		/*	use some permutations
										.add_parameter(0, new List_Range<PredictionType>()
															.builder()
															.add_option(Arrays.asList(PredictionType.ActivityPrediction))
															.add_option(Arrays.asList(PredictionType.TimePrediction))
															.add_option(Arrays.asList(PredictionType.ActivityPrediction, PredictionType.TimePrediction))
															.add_option(Arrays.asList(PredictionType.ActivityPrediction, PredictionType.RiskPrediction))
															.build()
										)
		*/
//										.add_parameter(1, new Numeric_Range<Integer>(1, 6, 10, 2, "a", "ai", Integer.class))
//										.add_parameter(2, new Numeric_Range<Integer>(2, 25, 25, 0, "b", "bi", Integer.class))
//										.add_parameter(3, new Numeric_Range<Integer>(3, 10, 10, 0, "c", "ci", Integer.class))
										
										.add_parameter(1, new Numeric_Range<Integer>(1, 2, 16, 1, Integer.class))
										.add_parameter(2, new Numeric_Range<Integer>(2, 15, 50, 1, Integer.class))
										.add_parameter(3, new Numeric_Range<Integer>(3, 2, 21, 1, Integer.class))
										.build_classifier()
				
										// ngram
										.add_classifier(NGramClassifier.class)
										.add_parameter(0, new List_Range<PredictionType>(0, PredictionType.class)
															.builder()
															.add_option(Arrays.asList(PredictionType.ActivityPrediction))
															.build()
												
										)
										// 1,2,8,1
										.add_parameter(1, new Numeric_Range<Integer>(1, 2, 100, 1, Integer.class)	)
										.build_classifier()
										.build();
		
	

		
		SaveDefinition saveD = new SaveDefinition().builder()
													.saveTopPercent(0.4, project_path, new Pair<String, Method>("ACC", Metric.class.getMethod("accuracy", null)))
													.saveTopPercentPerClassifer(0.25, project_path, new Pair<String, Method>("ACC", Metric.class.getMethod("accuracy", null)))
													.build();
		
		// Frotend -> show best or best 10x
		
		// need to implement reload of Executor
		
		//Either
//		Executor exe = new Executor(project_name, project_path, data, mc, c, saveD);
		Executor exe = new Executor(project_name, project_path, data, mc, c, saveD, 
				 new BayesianOptimizer(10, 0.01, Metric.class.getMethod("accuracy", null), 100, 25, SupportedClassifier.LinearRegression)
				// new RandomOptimizer(100, 0.99, Metric.class.getMethod("calculateAccuracy", null))
				);
		//or
		// Executor exe = new Executor(project_name, project_path, data, mc, configurations, saveD);

		//restart
//		Executor exe = Executor.Executor(project_name, project_path);
		
		
		// running shit Liste<String> projetknamen + Prozentzahl abschluss
		
		
//		Executor e = Executor.Executor(project_name, project_path);
		
		exe.start_execution();
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		// 1) whats currently running?!
		// 2) XES-Log to all Attributes
		// 3) Classifier aufbohren - all Values from XES?! encoding Erweiterung
//fin		// 4) DataPoint aufbohren - alles hat										-> Nico
		// 5) Classifier DataSet to Encoding  <- in Classifier zu regeln										
		
		// Suchraumoptimierung - Genetik / Gaus / Suchalgos							-> 
			// Genetik: Train/eval Data überall gleich ->
		
		// Metric Earlyness - how early does it get good Values   
		
		// 6) DataPreProcessing -> 
		// 		7.0) Cyclomatic Complexity etc.
		//		7.1) XES-Metriken
		// 		7.2) Simulation mit Agenten
		
		// 8) Cluster Data Prediction -> implementation innerhalb von MergedClassifer
		
		
		System.out.println("FIN");
		
	}
}
