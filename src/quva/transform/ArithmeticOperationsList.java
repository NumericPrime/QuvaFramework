package quva.transform;

/**This adds arithmetic operations as transformations. These are ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION*/
public interface ArithmeticOperationsList {
	/**This executes addition.<br>
	 * <b>Syntax</b>:<br>
	 * The two parts of the sum are the first arguments. The variable the result gets saved into comes third.<br>
	 * var1+var2=target*/
	public final static MapTransformation ADDITION=(m,sum1,sum2,target)->m.linearEquation(sum1+"+"+sum2+"-"+target);
	/**This executes subtraction.<br>
	 * <b>Syntax</b>:<br>
	 * The first argument is the minuent the second is the subtrahent. The third will save the result:<br>
	 * var1-var2=target*/
	public final static MapTransformation SUBTRACTION=(m,min,sub,target)->m.linearEquation(min+"-"+sub+"-"+target);
	/**This executes multiplication.<br>
	 * <b>Syntax</b>:<br>
	 * The two parts of the sum are the first arguments the factors. The variable the result gets saved into comes third<br>
	 * var1*var2=target*/
	public final static MapTransformation MULTIPLICATION=(m,fac1,fac2,target)->m.multiply(target,fac1,fac2);
	/**This executes division.<br>
	 * <b>Syntax</b>:<br>
	 * The first variable is the one that gets divided the second is the divisor. The variable the result gets saved into comes third.<br>
	 * var1/var2=target*/
	public final static MapTransformation DIVISION=(m,fac,div,target)->m.multiply(fac,div,target);
}
