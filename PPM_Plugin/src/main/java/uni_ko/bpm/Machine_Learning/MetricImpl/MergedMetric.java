package uni_ko.bpm.Machine_Learning.MetricImpl;

import uni_ko.bpm.Machine_Learning.Metric;

import java.util.ArrayList;
import java.util.List;

public class MergedMetric extends Metric {

    private List<Metric> metrics = new ArrayList<>();

    public MergedMetric() {
    }

    public MergedMetric(List<Metric> metrics) {
        this.metrics = metrics;
    }

    @Override
    public Double accuracy() {
        if (this.metrics.isEmpty()) {
            return 0.0;
        } else {
            return this.metrics.stream().mapToDouble(Metric::accuracy).sum() / this.metrics.size();
        }
    }

    @Override
    public Double MAE() {
        if (this.metrics.isEmpty()) {
            return 0.0;
        } else {
            return this.metrics.stream().mapToDouble(Metric::MAE).sum() / this.metrics.size();
        }
    }

    @Override
    public Double SSE() {
        if (this.metrics.isEmpty()) {
            return 0.0;
        } else {
            return this.metrics.stream().mapToDouble(Metric::SSE).sum();
        }
    }

    @Override
    public long getNoParameters() {
        if (this.metrics.isEmpty()) {
            return 0;
        } else {
            return this.metrics.get(0).getNoParameters();
        }
    }

    @Override
    public int getNoObservations() {
        if (this.metrics.isEmpty()) {
            return 0;
        } else {
            return this.metrics.get(0).getNoObservations();
        }
    }

    @Override
    public void update(List<Double[]> values) {
        throw new UnsupportedOperationException("Merged Metric does not allow updating. Please update for each Metric respectively.");
    }

}
