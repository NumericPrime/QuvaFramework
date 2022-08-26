package quva.transform;

import quva.core.QUBOMatrix;

/**Adds the option to add polynomial equations to the {@code QUBOMatrix}*/
public class QuvaPolynomialEquation implements SingleVarTransformation {
	public float[] parms;
	/**Here the coefficients of the polynomial equation are entered here
	 * @param params the coefficients of the polynomial*/
	public QuvaPolynomialEquation(float... params) {
		parms=params;
		count++;
	}
	@Override
	/**This will set the chosen polynomial to target<br>
	 * Note: this will create all carries necessary. The names of the associated variables are "pol"+_numberOfPolynomial_+"x"+_NumberOfVariable_*/
	@quva.core.Registers
	public void apply(QUBOMatrix m, String var) {
		m.applyAbstractTransformation(new QuvaPolynomial(parms), var, null, "0");
	}
	public static int count=0;
	@Override
	public String toString() {
		return "pol"+count;
	}
}
