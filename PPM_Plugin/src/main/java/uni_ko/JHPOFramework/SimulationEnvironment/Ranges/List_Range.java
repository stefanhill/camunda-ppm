package uni_ko.JHPOFramework.SimulationEnvironment.Ranges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class List_Range<T> extends Range<List<T>> {

    public boolean allow_multi_select;
    private List<T> option;

    // Cammunda plugin usage
    public List_Range(Integer parameter_id, String text, String info_text, Class<T> data_type) throws InstantiationException, IllegalAccessException {
        super(new ArrayList<List<T>>(), parameter_id, text, info_text, data_type);
    }

    public List_Range(Integer parameter_id, List<List<T>> options, String text, String info_text, Class<T> data_type) throws InstantiationException, IllegalAccessException {
        super(options, parameter_id, text, info_text, data_type);
    }

    public List_Range(Integer parameter_id, List<List<T>> options, String text, String info_text, Class<T> data_type, boolean allow_multi_select) throws InstantiationException, IllegalAccessException {
        super(options, parameter_id, text, info_text, data_type);
        this.allow_multi_select = allow_multi_select;
    }

    public List_Range(Integer parameter_id, List<T> option, boolean allow_multi_select, String text, String info_text, Class<T> data_type) throws Exception {
        super(new ArrayList<List<T>>(), parameter_id, text, info_text, data_type);

        this.allow_multi_select = allow_multi_select;
        this.option = option;
        this.calculateOptions();
    }

    // direct plugin useage
    public List_Range(Integer parameter_id, Class<T> data_type) throws InstantiationException, IllegalAccessException {
        super(new ArrayList<List<T>>(), parameter_id, "", "", data_type);
    }

    public List_Range(Integer parameter_id, List<List<T>> options, Class<T> data_type) throws InstantiationException, IllegalAccessException {
        super(options, parameter_id, "", "", data_type);
    }

    public List_Range(Integer parameter_id, List<T> option, boolean allow_multi_select, Class<T> data_type) throws Exception {
        super(new ArrayList<List<T>>(), parameter_id, "", "", data_type);

        this.allow_multi_select = allow_multi_select;
        this.option = option;
        this.calculateOptions();
    }

    public ListRangeBuilder<T> builder() {
        return new ListRangeBuilder<>(this);
    }

    @Override
    public void calculateOptions() throws Exception {
        if (this.option != null) {
            this.options = new ArrayList<>();
            if (this.allow_multi_select) {
                this.options = this.calculatePermutationsRec(this.option);
            } else {
                for (T o : this.option) {
                    this.options.add(new ArrayList<>(Arrays.asList(o)));
                }
            }
        }
    }

    private List<List<T>> calculatePermutationsRec(List<T> baseList) {
        if (baseList == null || baseList.isEmpty()) {
            throw new NullPointerException("Cannot calculate permutations for an empty list.");
        }
        if (baseList.size() > 20) {
            throw new UnsupportedOperationException("Deriving permutations for parameters with more than 20 options is not intended.");
        }
        if (baseList.size() == 1) {
            return new ArrayList<>(Arrays.asList(baseList));
        } else {
            T topElem = baseList.get(0);
            List<List<T>> subList = this.calculatePermutationsRec(new ArrayList<>(baseList.subList(1, baseList.size())));
            List<List<T>> permutationList = new ArrayList<>(subList);
            permutationList.add(new ArrayList<>(Arrays.asList(topElem)));
            for (List<T> subElem :
                    subList) {
                List<T> concatenatedList = new ArrayList<>(subElem);
                concatenatedList.add(topElem);
                permutationList.add(concatenatedList);
            }
            return permutationList;
        }
    }

}
