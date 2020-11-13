package uni_ko.JHPOFramework.SimulationEnvironment.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.List_Range;
import uni_ko.JHPOFramework.SimulationEnvironment.Ranges.Range;

public class ParameterBuilder {
    protected List<Integer> ids = new ArrayList<Integer>();
    protected List<Range> ranges = new ArrayList<Range>();

    private ConfigurationBuilder cb;

    public ParameterBuilder(ConfigurationBuilder cb) {
        this.cb = cb;
    }

    public ParameterBuilder add_parameter(Integer id, Range range) {
        this.ids.add(id);
        this.ranges.add(range);
        return this;
    }

    public ConfigurationBuilder add_parameter(HashMap<Integer, Range> parameters) throws Exception {
        for (Entry<Integer, Range> entry : parameters.entrySet()) {
            this.ids.add(entry.getKey());
            entry.getValue().calculateOptions();
            this.ranges.add(entry.getValue());
        }
        cb.add_pair(ids, ranges);
        return cb;
    }

    public ConfigurationBuilder build_classifier() {
        cb.add_pair(ids, ranges);
        return cb;
    }


}
