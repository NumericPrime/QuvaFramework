package quva.transform;

import quva.core.QUBOMatrix;

/**Adds calculating the value of a polynomial as a transformation*/
public class QuvaPolynomial implements FunctionTransformation {
	public float[] parms;
	int local_count=0;
	/**Here the coefficients of the polynomial equation are entered here
	 * @param params the coefficients of the polynomial*/
	public QuvaPolynomial(float... params) {
		parms=params;
		local_count=count;
		count++;
	}
	@Override
	/**This will set the chosen polynomial to target<br>
	 * Note: this will create all carries necessary. The names of the associated variables are "pol"+_numberOfPolynomial_+"x"+_NumberOfVariable_
	 * @param var name of the variable of the polynomial
	 * @param target location where the results are saved*/
	@quva.core.Registers
	public void apply(QUBOMatrix m, String var, String target) {
		int current_layer=m.layer;
		m.layer(current_layer+1);
		if(parms.length==1) m.linearEquation(parms[0]+"-"+target);
		if(parms.length==2) m.linearEquation(parms[0]+"+"+parms[1]+"*"+var+"-"+target);
		if(parms.length>2) {
			String base=parms[0]+"+"+parms[1]+"*"+var+"-"+target;
			PowerSeries s=new PowerSeries(Math.min(parms.length-1,m.find(var).length),toString()+"x"+(parms.length-1)+"<"+var+">");
			m.applyTransformation(s, var);
			for(int i=2;i<parms.length;i++) {
				if(i!=parms.length-1) s.lowerPower(i, toString()+"x"+i+"<"+var+">");
				/*if(i==2) {
					if(parms.length-1==i) m.registerMultiplyCarries(toString()+"x2", var, var);
					else m.registerMultiply(toString()+"x2", var, var);
					}
				if(i>2) {
					if(parms.length-1==i) m.registerMultiplyCarries(toString()+"x"+i,  toString()+"x"+(i-1), var);
					else m.registerMultiply(toString()+"x"+i, toString()+"x"+(i-1), var);
					}*/
				base+="+"+parms[i]+"*"+toString()+"x"+i+"<"+var+">";
			}
			//System.out.println(base);
			m.layer(current_layer);
			m.linearEquation(base);
		}
	}
	public static int count=0;
	@Override
	public String toString() {
		return "pol"+local_count;
	}
}
