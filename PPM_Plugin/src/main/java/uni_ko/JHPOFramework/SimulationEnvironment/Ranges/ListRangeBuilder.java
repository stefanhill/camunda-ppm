package uni_ko.JHPOFramework.SimulationEnvironment.Ranges;

import java.util.ArrayList;
import java.util.List;

public class ListRangeBuilder<T> {
	private List_Range<T> lr;
	
	public ListRangeBuilder(List_Range<T> lr) {
		this.lr = lr;
	}
	public ListRangeBuilder<T> add_option(List<T> option){
		this.lr.options.add(new ArrayList<T>(option));
		return this;
	}
	public List_Range<T> build() {
		return lr;
	}
}
