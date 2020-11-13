package uni_ko.bpm.Machine_Learning.WekaClassifier;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;
import weka.classifiers.trees.HoeffdingTree;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class HoeffdingClassifier extends Weka_Classifier {

    private List<String> leafPredictionStrategy;
    private List<String> splittingCriterion;
    private double splitError;
    private double splitForceThreshold;

    @Override
    public Metric train(Data_Set training_set, Data_Set test_set) throws Exception {
        this.last_calculated_metric = wk.train(training_set, test_set, this);
        return this.last_calculated_metric;
    }

    @Override
    public List<Parameter_Communication_Wrapper> configurational_parameters() {
        List<Parameter_Communication_Wrapper> parameters = new ArrayList<>();
        parameters.add(new Parameter_Communication_Wrapper_Lists<PredictionType>(
                        0,
                        "Prediction-Types",
                        "Defines which types this classifier will predict.",
                        false,
                        this.prediction_types,
                        PredictionType.class,
                        Arrays.asList(PredictionType.ActivityPrediction),
                        true
                )
        );
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                1,
                "Steps",
                "Steps back in the event log",
                false,
                this.steps,
                Integer.class,
                8)
        );
        parameters.add(new Parameter_Communication_Wrapper_Lists<String>(
                        2,
                        "Leaf prediction strategy",
                        "The leaf prediction strategy to use.",
                        false,
                        this.leafPredictionStrategy,
                        String.class,
                        Arrays.asList("majority class", "naive bayes", "naive bayes adaptive"),
                        false
                )
        );
        parameters.add(new Parameter_Communication_Wrapper_Lists<String>(
                        3,
                        "Splitting criterion",
                        "The splitting criterion to use.",
                        false,
                        this.splittingCriterion,
                        String.class,
                        Arrays.asList("gini", "info gain"),
                        false
                )
        );
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                4,
                "Splitting error",
                "The allowable error in a split decision - values closer to zero will take longer to decide.",
                false,
                this.splitError,
                Double.class,
                1e-7)
        );
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                5,
                "Split force threshold",
                "Threshold below which a split will be forced to break ties.",
                false,
                this.splitForceThreshold,
                Double.class,
                0.05)
        );
        return parameters;
    }

    @Override
    public void set_configurational_parameters(HashMap<Integer, Object> parameters) throws Exception {
        this.wk = new WekaUtilities();
        wk.models = new EnumMap<>(PredictionType.class);
        wk.fields = Arrays.asList("duration", "task_uid", "RiskQuadratic");
        try {
            this.prediction_types = ((List<String>) parameters.get(0)).stream()
                    .map(PredictionType::valueOf)
                    .collect(toList());
        } catch (Exception e) {
            this.prediction_types = ((List<PredictionType>) parameters.get(0));
        }

        this.leafPredictionStrategy = (List<String>) parameters.get(2);
        this.splittingCriterion = (List<String>) parameters.get(3);
        this.splitError = (double) parameters.get(4);
        this.splitForceThreshold = (double) parameters.get(5);
        String lpsValue;
        switch (this.leafPredictionStrategy.get(0).toLowerCase()) {
            case "naive bayes":
                lpsValue = "1";
                break;
            case "naive bayes adaptive":
                lpsValue = "2";
                break;
            default:
                lpsValue = "0";
        }
        String scValue = this.splittingCriterion.get(0).equals("info gain") ? "1" : "0";

        for (PredictionType predictionType : this.prediction_types) {
            HoeffdingTree ht = new HoeffdingTree();
            String[] options = new String[8];
            options[0] = "-L";
            options[1] = lpsValue;
            options[2] = "-S";
            options[3] = scValue;
            options[4] = "-E";
            options[5] = String.valueOf(this.splitError);
            options[6] = "-H";
            options[7] = String.valueOf(this.splitForceThreshold);
            try {
                ht.setOptions(options);
            } catch (Exception ignored) {
            }
            wk.models.put(predictionType, ht);
        }
        wk.trained = false;
        this.steps = (int) parameters.get(1);
        wk.steps = this.steps;
    }

}
