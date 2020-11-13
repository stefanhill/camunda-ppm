package uni_ko.bpm.Machine_Learning;

import uni_ko.bpm.Annotations.OptimizerOrdering;
import uni_ko.bpm.Annotations.OptimizerOrdering.OrderingOption;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for the Metric which is returned after training a classifier
 * @implNote See MetricImpl for the actual implementations
 */
public abstract class Metric implements Serializable {

    protected long noParameters = 0;

    @OptimizerOrdering(best = OrderingOption.High)
    public abstract Double accuracy();

    @OptimizerOrdering
    public Double AIC() {
        int N = this.getNoObservations();
        if (N > 0) {
            return N * Math.log(this.SSE() / N) + 2 * (this.noParameters + 1);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    @OptimizerOrdering
    public Double BIC() {
        int N = this.getNoObservations();
        if (N > 0) {
            return N * Math.log(this.SSE() / N) + 2 * Math.log(this.noParameters + 1d);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    @OptimizerOrdering
    public abstract Double MAE();

    @OptimizerOrdering
    public abstract Double SSE();

    /**
     * https://en.wikipedia.org/wiki/Root-mean-square_deviation
     *
     * @return root mean squared error
     */
    public Double RMSE() {
        if (this.getNoObservations() > 0) {
            return Math.sqrt(this.SSE() / this.getNoObservations());
        } else {
            return -1.0;
        }
    }

    public long getNoParameters() {
        return this.noParameters;
    }

    public abstract int getNoObservations();

    public abstract void update(List<Double[]> values);
}
