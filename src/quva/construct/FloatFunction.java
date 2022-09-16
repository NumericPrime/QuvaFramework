package quva.construct;

/**A function<br><pre>
 * f:R-&gt;R
 *   x-&gt;f(x)</pre>
 * This function can be implemented and applied to a variable using CalculateFunction
 * @see quva.construct.CalculateFunction*/
public interface FloatFunction {
	/**The function itself
	 * @param x the input of the function
	 * @return the result of the function*/
	public float f(float x);
}
