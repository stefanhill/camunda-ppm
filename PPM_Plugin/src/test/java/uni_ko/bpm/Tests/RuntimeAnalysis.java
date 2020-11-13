package uni_ko.bpm.Tests;

import uni_ko.JHPOFramework.Communication.CommunicationHandler;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Execution.Executor;
import uni_ko.JHPOFramework.SimulationEnvironment.Classifier_Serialization.SaveDefinition;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.Configuration;
import uni_ko.JHPOFramework.SimulationEnvironment.Configuration.ConfigurationBuilder;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RuntimeAnalysis {

    private static final String ACC = "accuracy";

    private static final String PROJECT_PATH = ".."+File.separator+"Simulations"+File.separator+"RuntimeAnalysis_" + System.currentTimeMillis() + File.separator;
    private static final String RESOURCE_PATH = ".."+File.separator+"Resources"+File.separator+"logs"+File.separator;
    private static final String PROJECT_PREFIX = "Simulation_";

    public static void main(String[] args) throws Exception {
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
                .saveTopPercent(0.01,
                        PROJECT_PATH,
                        new Pair<>("ACC", Metric.class.getMethod(ACC, null)))
                .saveTopPercentPerClassifer(0.01,
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
                "IBkClassifier"/*,
                "RegressionClassifier"*/);
        Double upper = 1.75;
        Double lower = 0.6;
        switch (configurationName) {
            case "small":
                upper = 1.5;
                lower = 0.8;
                break;
            case "large":
                upper = 2.0;
                lower = 0.4;
                break;
        }
        CommunicationHandler ch = new CommunicationHandler();
        ConfigurationBuilder configurationBuilder = new Configuration().builder();
        for (String classifierName :
                classifierNames) {

            List<Range> ranges = ch.getNeededRanges(classifierName, lower, upper);
            HashMap<Integer, Range> parameterMap = new HashMap<>();
            for (Range range : ranges) {
                parameterMap.put(range.parameter_id, range);
            }
            for (Class<? extends Classifier> cl : ReflectionReads.getPossibleClassifierClasses()) {
                if (cl.getSimpleName().equals(classifierName)) {
                    configurationBuilder.add_classifier(cl).add_parameter(parameterMap);
                }
            }

        }
        return configurationBuilder.build();
    }

    public static OptimizationAlgorithm getOptimizer(String optimizerName) throws Exception {
        switch (optimizerName) {
            case "BayesianOptimizer":
                return new BayesianOptimizer(10,
                        0.01,
                        Metric.class.getMethod(ACC, null),
                        100,
                        25,
                        BayesianOptimizer.SupportedClassifier.RandomForest);
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
