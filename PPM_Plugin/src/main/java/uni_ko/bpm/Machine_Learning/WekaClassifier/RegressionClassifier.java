package uni_ko.bpm.Machine_Learning.WekaClassifier;

import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Machine_Learning.Classification;
import uni_ko.bpm.Machine_Learning.Classifier;
import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.PredictionType;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Lists;
import uni_ko.bpm.Machine_Learning.Util.Parameter_Communication_Wrapper_Single;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class RegressionClassifier extends Weka_Classifier {

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
                "Steps back in the event log",
                false,
                this.steps,
                Integer.class,
                8)
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

        for (PredictionType predictionType : this.prediction_types) {
            if (FieldUtilities.isNominal(predictionType)) {
                wk.models.put(predictionType, new Logistic());
            } else if (FieldUtilities.isNumeric(predictionType)) {
                wk.models.put(predictionType, new LinearRegression());
            }
        }

        wk.trained = false;
        this.steps = (int) parameters.get(1);
        wk.steps = this.steps;
    }


}
