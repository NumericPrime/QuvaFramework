package quva.transform;

/**Here a list of special functions will be saved. Currently it has the RELU function, SQRT and SQ.*/
public interface FunctionsList {
	/**Executes the ReLu function
	 * Note: target must have <b>one qubit less</b> than var. And should have the same weights (except the <b>flipped</b> first one)*/
	public static final FunctionTransformation RELU=(m,var,target)->{
		int marker=m.find(var)[0];
		int[] targetBits=m.find(target);
		for(int i=0;i<m.find(var).length-1;i++) m.iLink(targetBits[i],m.find(var)[i+1],marker);
	};
	/**Gets the square root of a variable.*/
	public static final FunctionTransformation SQRT=(m,var,target)->{
		m.registerSquareCarries(var, target);
	};
	/**Gets the square of a variable.*/
	public static final FunctionTransformation SQ=(m,var,target)->{
		m.registerSquareCarries(target, var);
	};
}
