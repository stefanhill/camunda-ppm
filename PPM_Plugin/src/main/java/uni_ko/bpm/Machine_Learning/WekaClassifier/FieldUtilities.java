package uni_ko.bpm.Machine_Learning.WekaClassifier;

import uni_ko.bpm.Machine_Learning.PredictionType;
import weka.core.Attribute;

import java.util.List;

public class FieldUtilities {

    public static boolean isNominal(String field) {
        return field.equals("task_uid");
    }

    public static boolean isNominal(PredictionType pt) {
        return pt.equals(PredictionType.ActivityPrediction);
    }

    public static boolean isNumeric(String field) {
        return field.equals("duration") || field.equals("RiskLinear") || field.equals("RiskQuadratic");
    }

    public static boolean isNumeric(PredictionType pt) {
        return pt.equals(PredictionType.TimePrediction) || pt.equals(PredictionType.RiskPrediction);
    }

    public static String getFieldByPredictionType(PredictionType pt) {
        switch (pt) {
            case ActivityPrediction:
                return "task_uid";
            case TimePrediction:
                return "duration";
            case RiskPrediction:
                return "RiskQuadratic";
            default:
                return null;
        }
    }

    public static Attribute createAttribute(String field, int index, List<String> nominalValues) {
        return (FieldUtilities.isNominal(field)) ? new Attribute(field + index, nominalValues) : new Attribute(field + index);
    }

}
