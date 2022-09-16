package quva.construct;

import quva.core.QUBOMatrix;
import quva.core.QuvaDebug;
import quva.transform.FunctionTransformation;
/**This implements a function as a transformation if you have a function f and a variable x this will create a variable containing f(x)
 * @see quva.construct.QuvaConstruct
 * @see quva.construct.CalculatePattern*/
public class CalculateFunction implements FunctionTransformation{
	/**Function to be used*/
	public FloatFunction f;
	/**QuvaConstruct to be used*/
	public QuvaConstruct sc=null;
	/**This constructor takes the function to be processed. This will also create a Construct while applying the transformation.
	 * @param function function to be processed*/
	public CalculateFunction(FloatFunction function) {
		QuvaDebug.logprnt("CalculateFunction.create","Creating...");
		f=function;
	}
	/**This constructor takes the function to be processed and the construct to be used
	 * @param function function to be processed
	 * @param sc construct to be used*/
	public CalculateFunction(FloatFunction function,QuvaConstruct sc) {
		QuvaDebug.logprnt("CalculateFunction.create","Creating...");
		this.sc=sc;
		f=function;
	}
	/**{@inheritDoc}*/
	@Override
	@quva.core.Registers
	public void apply(QUBOMatrix m, String var, String target) {
		QuvaDebug.logprnt("CalculateFunction.apply","Applying on "+m.getClass().getSimpleName()+m.hashCode()+" "+var+" "+target);
		if(sc==null) sc=new SafeConstruct(var,m);
		new ImplementedFunction(m,sc,var,target).process(f);
		sc.process();
	}

}