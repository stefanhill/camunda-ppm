package uni_ko.bpm.Data_Management.DataSetCalculations;

import java.io.Serializable;

import uni_ko.bpm.Data_Management.Flow;

public abstract class DataSetCalculation implements Serializable {
	public abstract Flow calculate(Flow flow);
}
