package quva.transform;

import quva.core.QUBOMatrix;
/**This interface is used to add transformations that have three variables involved*/
public interface MapTransformation extends AbstractTransform {
	/**This is and empty implementation for single var transformations*/
	public default void apply(QUBOMatrix m,String var) {}
	/**This is a empty implementation for a function*/
	public default void apply(QUBOMatrix m,String var,String target) {}
	/**This method must be implemented to create a map*/
	public abstract void apply(QUBOMatrix m,String var1,String var2,String target);

}
