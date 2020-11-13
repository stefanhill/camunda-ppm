package uni_ko.bpm.Data_Management.Data_Reader;

import java.util.List;

import uni_ko.bpm.Data_Management.Data_Exception;
import uni_ko.bpm.Data_Management.Data_Point;
import uni_ko.bpm.Data_Management.Data_Set;
import uni_ko.bpm.Data_Management.Flow;
import uni_ko.bpm.Data_Management.DataSetCalculations.DataSetCalculation;

public class Token_Reader extends Data_Reader {
    private int token_size;
    private Data_Set local_set = null;
    private Data_Set permutated_set;
    private Data_Set fill_reverse_set;
    private int window_index_start;

    private List<DataSetCalculation> postProcessing;

    public Token_Reader(Data_Set data_set, int token_size, int window_index_start, List<DataSetCalculation> postProcessing) {
        super(data_set.get_process_definition_Id());
        this.token_size = token_size;
        this.local_set = data_set;
        this.window_index_start = window_index_start;
        this.postProcessing = postProcessing;
    }

    public Data_Set get_instance_flows() throws Data_Exception {
        if (this.permutated_set == null) {
            permutate_set(this.window_index_start, false);
        }
        return permutated_set;
    }

    public Data_Set get_instance_flows(int number_of_flows) throws Data_Exception {
        if (this.permutated_set == null) {
            permutate_set(this.window_index_start, false);
        }
        return this.limited_set(number_of_flows);
    }

    public Data_Set get_reverse_filled_instance_flows() throws Data_Exception {
        if (this.permutated_set == null) {
            permutate_set(this.window_index_start, true);
        }
        return permutated_set;
    }

    public int get_set_size() {
        return permutated_set.data_set.size();
    }

    private void permutate_set(int window_index_start, boolean fill_reverse) {
        this.permutated_set = new Data_Set(this.local_set.get_process_definition_Id(), this.postProcessing);
        this.permutated_set.set_unique_Tokens(this.local_set.get_unique_Task_Name());
        for (Flow flow : this.local_set.data_set) {
            for (int window_index = window_index_start; window_index < flow.get_length(); window_index++) {
                this.permutated_set.add_Flow(create_sub_flow(flow, window_index, fill_reverse));
            }
        }
    }

    private Flow create_sub_flow(Flow flow, int window_index, boolean fill_reverse) {
        Flow new_flow = new Flow(flow.get_process_Instance_Id());
        if (fill_reverse) {
            for (int i = 0; i + window_index - this.token_size + 1 < 0; i++) {
                new_flow.add_Data_Point(new Data_Point("Unknown"));
            }
            int start = window_index - this.token_size + 1;
            while (new_flow.get_length() < this.token_size) {
                if (start >= 0) {
                    new_flow.add_Data_Point(flow.get_flow().get(start));
                }
                start++;
            }
        } else {
            for (int start = window_index - this.token_size; start <= window_index; start++) {
                if (start >= 0) {
                    new_flow.add_Data_Point(flow.get_flow().get(start));
                }
            }
            while (new_flow.get_length() < this.token_size) {
                new_flow.add_Data_Point(new Data_Point("Unknown"));
            }
        }
        return new_flow;
    }

    private int counter = 0;

    private Data_Set limited_set(int number_of_flows) {
        Data_Set buffer_set = new Data_Set(this.process_Definition_Id, this.postProcessing);
        for (; number_of_flows > 0; number_of_flows--) {
            buffer_set.add_Flow(this.permutated_set.data_set.get(counter));
            counter = (counter < this.permutated_set.data_set.size() - 1) ? counter + 1 : 0;
        }
        return buffer_set;
    }

}
