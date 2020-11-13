package uni_ko.bpm.Annotations;


import java.lang.annotation.*;




@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface OptimizerOrdering {
	public enum OrderingOption{
		Low, 
		High;
	}
	OrderingOption best() default OrderingOption.Low;
}
