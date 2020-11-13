package uni_ko.bpm.Machine_Learning.MetricImpl;

import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.Metric_Exception;

import java.util.List;

public class BinaryMetric extends Metric {

    private Double truePositive;
    private Double falsePositive;
    private Double falseNegative;
    private Double trueNegative;
    private Double total;

    public BinaryMetric(Double truePositive, Double falsePositive, Double falseNegative, Double trueNegative, int noParameters) throws Metric_Exception {
        super();
        if (truePositive != null && falsePositive != null && falseNegative != null && trueNegative != null) {
            this.truePositive = truePositive;
            this.falsePositive = falsePositive;
            this.falseNegative = falseNegative;
            this.trueNegative = trueNegative;
            this.total = this.truePositive + this.falsePositive + this.falseNegative + this.trueNegative;
            this.noParameters = noParameters;
        } else {
            throw new Metric_Exception("Initialization of Metric is not possible with null values.");
        }
    }

    @Override
    public Double accuracy() {
        return (this.truePositive + this.trueNegative) / this.total;
    }


    @Override
    public Double MAE() {
        if (this.total > 0) {
            return (this.falseNegative + this.falsePositive) / this.total;
        }
        else {
            return -1.0;
        }
    }

    @Override
    public Double SSE() {
        return this.falsePositive + this.falseNegative;
    }

    @Override
    public void update(List<Double[]> values) {
        if (values != null) {
            for (Double[] valuePair :
                    values) {
                this.truePositive += valuePair[0];
                this.falsePositive += valuePair[1];
                this.falseNegative += valuePair[2];
                this.trueNegative += valuePair[3];
            }
        }
        this.total = this.truePositive + this.falsePositive + this.falseNegative + this.trueNegative;
    }

    @Override
    public int getNoObservations() {
        return Integer.parseInt(Double.toString(this.total));
    }

}
