package quva.util;

import quva.core.QUBOMatrix;

public class PolynimialEquationSolver extends QUBOMatrix implements QuvaTemplate {
	private static final long serialVersionUID = 585243803056714662L;

	PolynimialEquationSolver(float maxweight,int qubitCount,boolean flip,float[] coefficients){
		super(200);
		register("x",qubitCount,maxweight,flip);
		applyTransformation(new quva.transform.QuvaPolynomialEquation(coefficients),"x");
	}
	
	@Override
	public quva.core.QUBOMatrix run() {
		return this;
	}

}
