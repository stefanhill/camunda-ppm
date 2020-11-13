package uni_ko.JHPOFramework.RuntimeAnalysis;

import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.Configuration;
import uni_ko.JHPOFramework.SimulationEnvironment.Data.Data;
import uni_ko.JHPOFramework.SimulationEnvironment.Metric_Configuration.MetricConfiguration;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.BayesianOptimizer;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.GridOptimizer;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.OptimizationAlgorithm;
import uni_ko.JHPOFramework.SimulationEnvironment.Optimizer.RandomOptimizer;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;
import uni_ko.JHPOFramework.Structures.Pair;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Reflections.ReflectionReads;

import javax.management.InvalidAttributeValueException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class RuntimeAnalysis {

    private static final String ACC = "accuracy";

    private static final String PROJECT_PATH = "C:\\git\\fg-bks-camunda\\Simulations\\RuntimeAnalysis_" + System.currentTimeMillis() + "\\";
    private static final String RESOURCE_PATH = "C:\\git\\fg-bks-camunda\\Resources\\logs\\";
    private static final String PROJECT_PREFIX = "Simulation_";

    public static void main(String[] args) throws Exception {
        Class<?> test = ClassLoader.getSystemClassLoader().loadClass("uni_ko.bpm.Machine_Learning.WekaClassifier.WekaUtilities");
        //Class<?> hi = ClassLoader.getSystemClassLoader().loadClass("org.camunda.bpm.engine.history.HistoricActivityInstance");
        List<String> logNames = Arrays.asList("DomesticDeclarations", "InternationalDeclarations");
        List<String> optimizerNames = Arrays.asList("GridOptimizer", "RandomOptimizer", "BayesianOptimizer");
        List<String> configurationNames = Arrays.asList("small", "medium", "large");
        for (String logName :
                logNames) {
            for (String optimizerName :
                    optimizerNames) {
                for (String configurationName :
                        configurationNames) {
                    startSimulation(logName, optimizerName, configurationName);
                }
            }
        }
    }

    public static void startSimulation(String logName, String optimizerName, String configurationName) throws Exception {
        File projectDir = new File(PROJECT_PATH);
        projectDir.mkdir();
        Executor exe = new Executor(PROJECT_PREFIX + logName + "_" + optimizerName + "_" + configurationName,
                PROJECT_PATH,
                getData(logName),
                getMetricConfiguration(),
                getConfiguration(configurationName),
                getSaveDefinition(),
                getOptimizer(optimizerName));
        exe.start_execution();
    }

    public static Data getData(String logName) throws Data_Exception {
        return new Data().builder()
                .addData(Data_Set.import_XES(RESOURCE_PATH + logName + ".xes", null), 0.8, logName);
    }

    public static MetricConfiguration getMetricConfiguration() throws NoSuchMethodException, InvalidAttributeValueException {
        return new MetricConfiguration()
                .addMetric("ACC", Metric.class.getMethod(ACC, null))
                .addMetric("AIC", Metric.class.getMethod("AIC", null))
                .addMetric("BIC", Metric.class.getMethod("BIC", null))
                .addMetric("MAE", Metric.class.getMethod("MAE", null))
                .addMetric("SSE", Metric.class.getMethod("SSE", null))
                .addMetric("RMSE", Metric.class.getMethod("RMSE", null));
    }

    public static SaveDefinition getSaveDefinition() throws NoSuchMethodException, UnsupportedEncodingException, InvalidAttributeValueException {
        return new SaveDefinition().builder()
                .saveTopPercent(0.4,
                        PROJECT_PATH,
                        new Pair<>("ACC", Metric.class.getMethod(ACC, null)))
                .saveTopPercentPerClassifer(0.25,
                        PROJECT_PATH,
                        new Pair<>("ACC", Metric.class.getMethod(ACC, null)))
                .build();
    }

    public static Configuration getConfiguration(String configurationName) throws Exception {
        List<String> classifierNames;
        classifierNames = Arrays.asList("NGramClassifier",
                "RandomForestClassifier",
                "NaiveBayesClassifier",
                "HoeffdingClassifier",
                "IBkClassifier",
                "RegressionClassifier");
        Double upper = 1.75;
        Double lower = 0.5;
        switch (configurationName) {
            case "small":
                upper = 1.5;
                lower = 0.75;
                break;
            case "large":
                upper = 2.5;
                lower = 0.25;
                break;
        }
        CommunicationHandler ch = new CommunicationHandler();
        HashMap<Class<? extends Classifier>, Pair<List<Integer>, List<Range>>> configurations = new HashMap<>();
        for (String classifierName :
                classifierNames) {

            List<Range> ranges = ch.getNeededRanges(classifierName, upper, lower);

            List<Integer> ids = ranges.stream().map(range -> range.parameter_id).collect(Collectors.toList());
            Class<? extends Classifier> classifier = null;
            for (Class<? extends Classifier> cC : ReflectionReads.getPossibleClassifierClasses()) {
                if (cC.getSimpleName().contentEquals(classifierName)) {
                    classifier = cC;
                    break;
                }
            }
            configurations.put(classifier,
                    new Pair<>(ids, ranges));
        }
        return new Configuration(configurations);
    }

    public static OptimizationAlgorithm getOptimizer(String optimizerName) throws Exception {
        switch (optimizerName) {
            case "BayesianOptimizer":
                return new BayesianOptimizer(10,
                        0.01,
                        Metric.class.getMethod(ACC, null),
                        100,
                        25,
                        BayesianOptimizer.SupportedClassifier.LinearRegression);
            case "RandomOptimizer":
                return new RandomOptimizer(100,
                        0.99,
                        Metric.class.getMethod(ACC, null));
            case "GridOptimizer":
                return new GridOptimizer();
            default:
                return null;
        }
    }
}
