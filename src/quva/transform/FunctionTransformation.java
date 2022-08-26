package quva.transform;

import quva.core.QUBOMatrix;
/**This interface is used to create Functions*/
public interface FunctionTransformation extends AbstractTransform {
	/**This is and empty implementation for single var transformations*/
	public default void apply(QUBOMatrix m,String var) {}
	/**This is method must be implemented to create a function*/
	public abstract void apply(QUBOMatrix m,String var,String target);
	/**This is an empty implementation for a map*/
	public default void apply(QUBOMatrix m,String var1,String var2,String target) {}

}
