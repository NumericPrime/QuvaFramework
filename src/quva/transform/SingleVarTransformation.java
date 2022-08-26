package quva.transform;

import quva.core.QUBOMatrix;

/**This interface allows transforming a single variable*/
public interface SingleVarTransformation extends AbstractTransform {
	/**This method must be implemented to create the transformation*/
	public abstract void apply(QUBOMatrix m,String var);
	/**This is a empty implementation for a function*/
	public default void apply(QUBOMatrix m,String var,String target) {}
	/**This is an empty implementation for a map*/
	public default void apply(QUBOMatrix m,String var1,String var2,String target) {}
}
