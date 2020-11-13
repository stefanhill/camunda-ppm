package uni_ko.bpm.Machine_Learning.WekaClassifier;

import no.uib.cipr.matrix.MatrixNotSPDException;
import uni_ko.bpm.Data_Management.DataSetCalculations.NaN.ReplaceNaN;
import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Reader.Token_Reader;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;
import uni_ko.bpm.Data_Management.DataSetCalculations.FieldCreation.TaskUID;
import uni_ko.bpm.Data_Management.DataSetCalculations.RiskCalculations.RiskLinear;
import uni_ko.bpm.Data_Management.DataSetCalculations.RiskCalculations.RiskQuadratic;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.MetricImpl.MergedMetric;
import uni_ko.bpm.Machine_Learning.MetricImpl.NumericMetric;
import uni_ko.bpm.Machine_Learning.MetricImpl.UnaryMetric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WekaUtilities implements Serializable {

    private static final long serialVersionUID = 4859950016071863925L;

    protected transient Map<PredictionType, weka.classifiers.Classifier> models;
    protected List<String> fields;
    protected int steps;
    protected boolean trained;
    private int missingId;
    private List<String> nominalValues;
    private HashMap<String, Integer> uniqueTaskNames;

    public Metric train(Data_Set training_set, Data_Set test_set, Classifier parent) throws Exception {
        List<Metric> metrics = new ArrayList<>();
        for (PredictionType pt : parent.get_prediction_type()) {
            Instances trainingInstances = this.datasetToInstances(training_set, pt, false);
            Instances testingInstances = this.datasetToInstances(test_set, pt, false);
            weka.classifiers.Classifier model = models.get(pt);
            if (model != null) {
                trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
                testingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
                if (model instanceof weka.classifiers.UpdateableClassifier && trained) {
                    for (Instance trainingInstance :
                            trainingInstances) {
                        ((UpdateableClassifier) model).updateClassifier(trainingInstance);
                    }
                } else {
                    try {
                        model.buildClassifier(trainingInstances);
                    }
                    catch (MatrixNotSPDException e) {
                        parent.last_calculated_metric = new MergedMetric(new ArrayList<>());
                        return parent.last_calculated_metric;
                    }
                }
                Metric metric = this.test(testingInstances, pt, model);
                metrics.add(metric);
            }
        }
        this.trained = true;
        parent.last_calculated_metric = new MergedMetric(metrics);
        return parent.last_calculated_metric;
    }

    public Metric test(Instances testingInstances, PredictionType pt, weka.classifiers.Classifier model) throws Exception {
        Metric metric = null;
        List<Double[]> metricParameters = new ArrayList<>();
        for (Instance instance :
                testingInstances) {
            double actualClass = instance.classValue();
            double predClass = model.classifyInstance(instance);
            if (FieldUtilities.isNominal(pt)) {
                if (metric == null) {
                    metric = new UnaryMetric(0.0, 0.0, testingInstances.numAttributes());
                    metricParameters.add(new Double[]{0.0, 0.0});
                }
                String actual = testingInstances.classAttribute().value((int) actualClass);
                String pred = testingInstances.classAttribute().value((int) predClass);
                if (actual.equals(pred)) {
                    metricParameters.get(0)[0]++;
                } else {
                    metricParameters.get(0)[1]++;
                }
            } else if (FieldUtilities.isNumeric(pt)) {
                if (metric == null) {
                    metric = new NumericMetric(new ArrayList<>(), testingInstances.numAttributes());
                }
                metricParameters.add(new Double[]{actualClass, predClass});
            }
        }
        if (metric != null) {
            metric.update(metricParameters);
        }
        return metric;
    }

    public List<Classification> evaluate(Data_Set data_set, Classifier parent) throws Exception {
        List<Classification> classifications = new ArrayList<>();
        for (PredictionType pt : parent.get_prediction_type()) {
            Instances instances = this.datasetToInstances(data_set, pt, true);
            // TODO: 27.08.2020 Hotfix: remove later? 
            while (instances.numInstances() > 1) {
                instances.delete(0);
            }
            int activityClass = instances.numAttributes() - 1;
            Map<Integer, String> activityMapping = new HashMap<>();
            for (int i = 0; i < instances.numDistinctValues(activityClass); i++) {
                activityMapping.put(i, instances.attribute(activityClass).value(i));
            }
            instances.setClassIndex(instances.numAttributes() - 1);
            weka.classifiers.Classifier model = this.models.get(pt);
            for (Instance instance :
                    instances) {
                if (model != null) {
                    HashMap<String, Float> evals = new HashMap<>();
                    if (FieldUtilities.isNominal(pt)) {
                        double[] distribution = model.distributionForInstance(instance);
                        evals = (HashMap<String, Float>) this.distributionToMap(distribution);
                    } else if (FieldUtilities.isNumeric(pt)) {
                        double pred = model.classifyInstance(instance);
                        evals.put(String.valueOf(instance.value(activityClass)), (float) pred);
                    }
                    evals.remove(null);
                    classifications.add(new Classification(evals, pt));
                }
            }
        }
        return classifications;
    }

    /**
     * Builds an instance set which is interpretable for Weka Classifiers
     * The instance set contains vectors in the following sequential encoding:
     * <p>
     * v = (a1, a2, ..., f1, f2, ..., c)
     * where the a's are mandatory and include a sequence of activities of length this.steps
     * f values contain all numeric fields with summed up durations or risk values
     *
     * @param ds             data set to train on
     * @param predictionType prediction type for labelling
     * @param cutRunning     whether it is a prediction (1) or a training (0)
     * @return tokenized weka Instances
     * @throws IllegalAccessException when converting to weka
     * @throws NoSuchFieldException   when converting to weka
     * @throws Data_Exception         when postprocessing data
     */
    public Instances datasetToInstances(Data_Set ds, PredictionType predictionType, boolean cutRunning) throws IllegalAccessException, NoSuchFieldException, Data_Exception {
        int cut = cutRunning ? 1 : 0;
        ds.setPostProcessing(Collections.singletonList(new RiskQuadratic()));
        ds.postProcess();
        Token_Reader reader = new Token_Reader(ds,
                this.steps - cut,
                0,
                Arrays.asList(new TaskUID(ds), new ReplaceNaN("RiskQuadratic", Double.class, 0.0))
        );

        Data_Set tokenSet = reader.get_reverse_filled_instance_flows();
        tokenSet.postProcess();
        String predictionField = FieldUtilities.getFieldByPredictionType(predictionType);

        if (this.nominalValues == null) {
            this.nominalValues = tokenSet.get_unique_ids();
            this.uniqueTaskNames = tokenSet.get_unique_Task_Name();
            this.missingId = this.uniqueTaskNames.get("Unknown");
        }

        ArrayList<Attribute> attributes = new ArrayList<>();
        int i = 0;
        while (i < this.steps - 1) {
            attributes.add(FieldUtilities.createAttribute("task_uid", i, this.nominalValues));
            i++;
        }
        for (String field : this.fields) {
            if (!field.equals("task_uid")) {
                attributes.add(FieldUtilities.createAttribute(field, 0, this.nominalValues));
            }
        }
        attributes.add(FieldUtilities.createAttribute(predictionField, i, this.nominalValues));

        Instances instances = new Instances("Instances", attributes, 0);

        Map<String, Iterator<List<Object>>> iterators = new HashMap();
        for (String field : this.fields) {
            iterators.put(field, tokenSet.get_set_parameter(field).iterator());
        }
        HashMap<String, Integer> utnToken = tokenSet.get_unique_Task_Name();
        // iterate over multiple Lists simultaniously ~
        int j = 0;
        while (iterators.values().stream().map(Iterator::hasNext).reduce((a, b) -> a && b).orElse(false)) {
            Map<String, List<Object>> fieldLists = new HashMap<>();
            for (String field : this.fields) {
                fieldLists.put(field, iterators.get(field).next());
            }

            // add all activities
            List<String> stringInstance = fieldLists.get("task_uid").subList(0, this.steps - 1)
                    .stream()
                    .map(x -> this.refactorUniqueTaskName(x, utnToken))
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // all other f values
            for (String field : this.fields) {
                if (!field.equals("task_uid")) {
                    stringInstance.add(fieldLists.get(field)
                            .subList(0, this.steps - 1)
                            .stream()
                            .map(Object::toString)
                            .map(Double::valueOf)
                            .reduce(Double::sum)
                            .get()
                            .toString());
                }
            }

            // add class label
            if (cutRunning) {
                stringInstance.add("0.0");
            } else {
                stringInstance.add(fieldLists.get(predictionField).get(this.steps - 1).toString());
            }
            double[] instanceValues = stringInstance
                    .stream()
                    .mapToDouble(Double::valueOf)
                    .toArray();

            Instance instance = new DenseInstance(1.0, instanceValues);
            instances.add(instance);
            j++;
            //instance.setDataset(instances);
        }
        return instances;
    }

    public Map<String, Float> distributionToMap(double[] distribution) {
        HashMap<String, Float> distributionMap = new HashMap<>();
        for (int i = 0; i < distribution.length; i++) {
            distributionMap.put(this.getUniqueTaskNameReverse(i, this.uniqueTaskNames), (float) distribution[i]);
        }
        return distributionMap;
    }

    public String getUniqueTaskNameReverse(Integer value, HashMap<String, Integer> uniqueTaskNames) {
        for (String taskName :
                uniqueTaskNames.keySet()) {
            if (uniqueTaskNames.get(taskName).equals(value)) {
                return taskName;
            }
        }
        return null;
    }

    public Object refactorUniqueTaskName(Object value, HashMap<String, Integer> uniqueTaskNamesRunning) {
        if (value != null) {
            String taskName = this.getUniqueTaskNameReverse(((Integer) value), uniqueTaskNamesRunning);
            return (Object) (double) this.uniqueTaskNames.get(taskName);
        } else {
            return null;
        }
    }

    public String path_manipulator(String path, int model_counter, Classifier parent) {
        int index = path.lastIndexOf(File.separator);
        return new StringBuilder(path).replace(index, index + 1, File.separator+"WekaClassifier"+File.separator + model_counter + "-" + parent.get_version() + "-").toString();
    }

}
