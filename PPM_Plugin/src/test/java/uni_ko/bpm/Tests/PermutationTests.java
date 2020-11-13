package uni_ko.bpm.Tests;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.List_Range;
import uni_ko.bpm.Machine_Learning.PredictionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermutationTests {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        List_Range lr = new List_Range<PredictionType>(0, PredictionType.class)
                .builder()
                .add_option(Arrays.asList(PredictionType.ActivityPrediction, PredictionType.TimePrediction, PredictionType.RiskPrediction))
                .build();

        //lr.calculatePermutationsRec(Arrays.asList(PredictionType.ActivityPrediction, PredictionType.TimePrediction, PredictionType.RiskPrediction));
        System.out.println("fini");
    }
}
