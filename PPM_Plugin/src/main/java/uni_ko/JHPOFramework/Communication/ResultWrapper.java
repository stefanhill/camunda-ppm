package uni_ko.JHPOFramework.Communication;

import java.util.HashMap;

import uni_ko.JHPOFramework.Structures.Pair;

public class ResultWrapper {
    public String classifierName;
    public Integer runID;
    public Pair<String, Double> metric;
    public HashMap<String, Object> configuration;
    public Integer runTime;

    public ResultWrapper(String classifierName,
                         Integer runID,
                         Pair<String, Double> metric,
                         HashMap<String, Object> configuration,
                         Integer runTime
    ) {
        super();
        this.classifierName = classifierName;
        this.runID = runID;
        this.metric = metric;
        this.configuration = configuration;
        this.runTime = runTime;
    }


}
