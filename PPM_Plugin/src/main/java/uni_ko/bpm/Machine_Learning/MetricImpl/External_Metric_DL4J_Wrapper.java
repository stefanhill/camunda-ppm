package uni_ko.bpm.Machine_Learning.MetricImpl;

import org.nd4j.evaluation.BaseEvaluation;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import uni_ko.bpm.Machine_Learning.Metric;

import java.util.List;

public class External_Metric_DL4J_Wrapper extends Metric {

    private static final long serialVersionUID = 1229706138345590251L;

    private BaseEvaluation<?>[] evaluation;
    private final RegressionEvaluation re;

    public External_Metric_DL4J_Wrapper(BaseEvaluation<?>[] baseEvaluations, long numParameters) {
        this.evaluation = baseEvaluations;
        this.re = (RegressionEvaluation) baseEvaluations[1];

        this.noParameters = numParameters;
    }

    @Override
    public Double accuracy() {
        return this.evaluation[0].getValue(Evaluation.Metric.ACCURACY);
    }

    @Override
    public Double MAE() {
    	return this.re.getValue(RegressionEvaluation.Metric.MAE);
    }

    @Override
    public Double SSE() {
        return this.re.getSumSquaredErrorsPerColumn().sum(0).getDouble(0);
    }


    @Override
    public void update(List<Double[]> values) {
        throw new UnsupportedOperationException("DL4J Wrapper Metric does not allow updating.");
    }

    @Override
    public int getNoObservations() {
        return this.re.getColumnNames().size();
    }

}
