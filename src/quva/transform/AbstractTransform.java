package quva.transform;

import quva.core.QUBOMatrix;

/**The Transformation API give an opportunity to use custom operation to a variable or multiple variables. These may range from calculation the power of a variable, or things like the RELU function or arithmetic operations.<br> This interface is not supposed to be implemented directly. Rather one of the three sub-interfaces should be used.
 * @see quva.transform.SingleVarTransformation
 * @see quva.transform.FunctionTransformation
 * @see quva.transform.MapTransformation
 * @see FunctionsList
 * @see PowerSeries
 * @see QuvaPolynomialEquation
 * @see ArithmeticOperationsList*/
public interface AbstractTransform {
	/**Transforms a single variable
	 * @param m {@code QUBOMatrix} to be manipulated
	 * @param var variable to be manipulated*/
	public void apply(QUBOMatrix m,String var);
	/**Transforms two variable (this can be used for functions)
	 * @param m {@code QUBOMatrix} to be manipulated
	 * @param src source variable
	 * @param target target variable*/
	public void apply(QUBOMatrix m,String src,String target);
	/**Transforms three variable (this can be used for functions with two inputs)
	 * @param m {@code QUBOMatrix} to be manipulated
	 * @param src1 first input
	 * @param src2 second input
	 * @param target target variable*/
	public void apply(QUBOMatrix m,String src1,String src2,String target);
}
