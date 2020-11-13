package uni_ko.bpm.Machine_Learning.MetricImpl;

import uni_ko.bpm.Machine_Learning.Metric;
import uni_ko.bpm.Machine_Learning.Metric_Exception;

import java.util.List;

public class UnaryMetric extends Metric {

    private Double trueClassifications;
    private Double falseClassifications;
    private Double total;

    public UnaryMetric(Double trueClassifications, Double falseClassifications, int noParameters) throws Metric_Exception {
        super();
        if (trueClassifications != null && falseClassifications != null) {
            this.trueClassifications = trueClassifications;
            this.falseClassifications = falseClassifications;
            this.total = trueClassifications + falseClassifications;
            this.noParameters = noParameters;
        } else {
            throw new Metric_Exception("Initialization of Metric is not possible with null values.");
        }
    }

    @Override
    public Double accuracy() {
        return this.trueClassifications / this.total;
    }

    @Override
    public Double MAE() {
        if (this.total > 0) {
            return this.falseClassifications / this.total;
        }
        else {
            return -1.0;
        }
    }

    @Override
    public void update(List<Double[]> values) {
        if (values != null) {
            for (Double[] valuePair :
                    values) {
                this.trueClassifications += valuePair[0];
                this.falseClassifications += valuePair[1];
            }
        }
        this.total = this.trueClassifications + this.falseClassifications;
    }

    @Override
    public Double SSE() {
        return this.falseClassifications;
    }

    @Override
    public int getNoObservations() {
    	return this.total.intValue();
    }

}
