package uni_ko.bpm.Machine_Learning.WekaClassifier;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;
import weka.classifiers.trees.RandomForest;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class RandomForestClassifier extends Weka_Classifier {

    private int iterations;
    private int depth;

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
                        Arrays.asList(PredictionType.ActivityPrediction, PredictionType.TimePrediction, PredictionType.RiskPrediction),
                        true
                )
        );
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                1,
                "Steps",
                "Steps back in the event log.",
                false,
                this.steps,
                Integer.class,
                8)
        );
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                2,
                "Iterations",
                "Number of iterations (i.e., the number of trees in the random forest).",
                false,
                this.iterations,
                Integer.class,
                25)
        );
        parameters.add(new Parameter_Communication_Wrapper_Single<>(
                3,
                "Max depth",
                "Set the maximum depth of the tree, 0 for unlimited.",
                false,
                this.depth,
                Integer.class,
                10)
        );
        return parameters;
    }

    @Override
    public void set_configurational_parameters(HashMap<Integer, Object> parameters) {
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

        this.iterations = (int) parameters.get(2);
        this.depth = (int) parameters.get(3);
        for (PredictionType predictionType : this.prediction_types) {
            RandomForest rf = new RandomForest();
            String[] options = new String[4];
            options[0] = "-I";
            options[1] = String.valueOf(this.iterations);
            options[2] = "-depth";
            options[3] = String.valueOf(this.depth);
            try {
                rf.setOptions(options);
            } catch (Exception ignored) {
            }
            wk.models.put(predictionType, rf);
        }
        wk.trained = false;
        this.steps = (int) parameters.get(1);
        wk.steps = this.steps;
    }

}
