package uni_ko.bpm.Machine_Learning.MetricImpl;

import uni_ko.bpm.Machine_Learning.Metric;

import java.util.List;

/**
 * see https://en.wikipedia.org/wiki/Accuracy_and_precision for further details
 */
public class NumericMetric extends Metric {

    // list of value pairs [actual, predicted]
    private final List<Double[]> valuePairs;

    public NumericMetric(List<Double[]> valuePairs, int noParameters) {
        this.valuePairs = valuePairs;
        this.noParameters = noParameters;
    }

    /**
     * Usually, accuracy is no metric for numeric outcomes.
     * This method calculates accuracy = correct / all
     * with correct = all value pairs where distance lower than rmse
     *
     * @return accuracy
     */
    @Override
    public Double accuracy() {
        if (!this.valuePairs.isEmpty()) {
            double rmse = this.RMSE();
            int correct = (int) this.valuePairs.stream()
                    .filter(x -> Math.abs(x[0] - x[1]) <= rmse).count();
            return (double) correct / this.valuePairs.size();
        } else {
            return -1.0;
        }
    }

    @Override
    public Double MAE() {
        if (!this.valuePairs.isEmpty()) {
            return this.valuePairs.stream().mapToDouble(d -> Math.abs(d[1] - d[0])).sum() / this.valuePairs.size();
        }
        else {
            return -1.0;
        }
    }

    @Override
    public int getNoObservations() {
        return valuePairs.size();
    }

    @Override
    public void update(List<Double[]> values) {
        this.valuePairs.addAll(values);
    }


    @Override
    public Double SSE() {
        if (!this.valuePairs.isEmpty()) {
            return this.valuePairs.stream().mapToDouble(d -> Math.pow(d[1] - d[0], 2)).sum();
        } else {
            return 0.0;
        }
    }

}
