package uni_ko.bpm.Tests;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.MetricImpl.MergedMetric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.WekaClassifier.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;
import weka.core.Capabilities;
import weka.core.Randomizable;

import java.util.*;

public class WekaTesting {

    public static void main(String[] args) throws Exception {
        //WekaTesting.capabilityTesting();
        String path = "C:\\git\\fg-bks-camunda\\Resources\\fp-camunda\\xes\\test01.xes";
        //WekaTesting.testRF(path);
        //WekaTesting.testHT(path);
        //WekaTesting.testIBk(path);
        //WekaTesting.testKStar(path);
        //WekaTesting.testNB(path);
        WekaTesting.testReg(path);
    }

    public static void testRF(String path) throws Exception {
        System.out.println("Random Forest");
        Data_Set ds = Data_Set.import_XES(path, null);
        Classifier rf = new RandomForestClassifier();
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(0, Arrays.asList("ActivityPrediction", "TimePrediction", "RiskPrediction"));
        parameters.put(1, 8);
        parameters.put(2, 50);
        parameters.put(3, 10);
        rf.set_configurational_parameters(parameters);
        MergedMetric m =(MergedMetric) rf.train(ds);
        System.out.println(m.accuracy());
        List<Classification> classifications = rf.evaluate(ds);
    }

    public static void testHT(String path) throws Exception {
        System.out.println("Hoeffding Tree");
        Data_Set ds = Data_Set.import_XES(path, null);
        Classifier ht = new HoeffdingClassifier();
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(0, Collections.singletonList("ActivityPrediction"));
        parameters.put(1, 8);
        parameters.put(2, Collections.singletonList("naive bayes adaptive"));
        parameters.put(3, Collections.singletonList("gini"));
        parameters.put(4, 1e-7);
        parameters.put(5, 0.1);
        ht.set_configurational_parameters(parameters);
        Metric m = ht.train(ds);
        System.out.println(m.accuracy());
        List<Classification> classifications = ht.evaluate(ds);
    }

    public static void testIBk(String path) throws Exception {
        System.out.println("IBk");
        Data_Set ds = Data_Set.import_XES(path, null);
        Classifier ibk = new IBkClassifier();
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(0, Arrays.asList("ActivityPrediction", "TimePrediction", "RiskPrediction"));
        parameters.put(1, 8);
        ibk.set_configurational_parameters(parameters);
        Metric m = ibk.train(ds);
        System.out.println(m.accuracy());
        List<Classification> classifications = ibk.evaluate(ds);
    }

    public static void testKStar(String path) throws Exception {
        System.out.println("KStar");
        Data_Set ds = Data_Set.import_XES(path, null);
        Classifier kstar = new KStarClassifier();
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(0, Arrays.asList("ActivityPrediction"/*, "TimePrediction", "RiskPrediction"*/));
        parameters.put(1, 8);
        kstar.set_configurational_parameters(parameters);
        Metric m = kstar.train(ds);
        System.out.println(m.accuracy());
        List<Classification> classifications = kstar.evaluate(ds);
    }

    public static void testNB(String path) throws Exception {
        System.out.println("Naive Bayes");
        Data_Set ds = Data_Set.import_XES(path, null);
        Classifier nb = new NaiveBayesClassifier();
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(0, Collections.singletonList(PredictionType.ActivityPrediction));
        parameters.put(1, 8);
        nb.set_configurational_parameters(parameters);
        Metric m = nb.train(ds);
        System.out.println(m.accuracy());
        List<Classification> classifications = nb.evaluate(ds);
    }

    public static void testReg(String path) throws Exception {
        System.out.println("Regression");
        Data_Set ds = Data_Set.import_XES(path, null);
        Classifier reg = new RegressionClassifier();
        HashMap<Integer, Object> parameters = new HashMap<>();
        parameters.put(0, Arrays.asList("ActivityPrediction", "TimePrediction", "RiskPrediction"));
        parameters.put(1, 12);
        reg.set_configurational_parameters(parameters);
        Metric m = reg.train(ds);
        System.out.println(m.accuracy());
        List<Classification> classifications = reg.evaluate(ds);
    }

    public static void capabilityTesting() {
        Map<String, weka.classifiers.Classifier> classifiers = new HashMap<>();
        classifiers.put("DecisionStump", new DecisionStump());
        classifiers.put("HoeffdingTree", new HoeffdingTree());
        classifiers.put("IBk", new IBk());
        classifiers.put("J48", new J48());
        classifiers.put("KStar", new KStar());
        classifiers.put("LinearRegression", new LinearRegression());
        classifiers.put("LMT", new LMT());
        classifiers.put("Logistic", new Logistic());
        classifiers.put("M5P", new M5P());
        classifiers.put("NaiveBayes", new NaiveBayes());
        classifiers.put("NaiveBayesMultinomial", new NaiveBayesMultinomial());
        classifiers.put("RandomCommitee", new RandomCommittee());
        classifiers.put("RandomForest", new RandomForest());
        classifiers.put("RandomSubSpace", new RandomSubSpace());
        classifiers.put("RandomTree", new RandomTree());
        classifiers.put("REPTree", new REPTree());
        classifiers.put("SGD", new SGD());
        classifiers.put("SMO", new SMO());
        classifiers.put("SMOreg", new SMOreg());

        for (String classifierName :
                classifiers.keySet()) {
            weka.classifiers.Classifier c = classifiers.get(classifierName);
            Capabilities cap = c.getCapabilities();
            Iterator<Capabilities.Capability> caps = cap.capabilities();
            if (!cap.hasDependencies()) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println(classifierName);
                while (caps.hasNext()) {
                    Capabilities.Capability capability = caps.next();
                    if (Arrays.asList("nominal attributes", "numeric attributes", "nominal class", "numeric class").contains(capability.toString().toLowerCase())) {
                        System.out.println(capability);
                    }
                }
                System.out.println("Random " + (c instanceof Randomizable));
            }
        }
    }

/*
    AdaBoostM1 m1 = new AdaBoostM1();
                    m1.setClassifier(new HoeffdingTree());
                    m1.setNumIterations(10);
    Bagging bagger = new Bagging();
                    bagger.setClassifier(m1);
                    bagger.setNumIterations(2);
    Stacking stacker = new Stacking();
                    stacker.setMetaClassifier(new J48());
    weka.classifiers.Classifier[] classifiers = {
            new NaiveBayes(),
            new RandomForest(),
            m1,
            bagger
    };
                    stacker.setClassifiers(classifiers);
    model = stacker;
*/
}
