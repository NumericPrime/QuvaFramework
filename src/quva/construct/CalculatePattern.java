package quva.construct;

import quva.core.QUBOMatrix;
import quva.core.QuvaDebug;
import quva.transform.FunctionTransformation;

import quva.core.QuvaDebug;
import quva.transform.FunctionTransformation;
/**This implements a pattern as a transformation if you have a function f and a variable x this will create a variable containing f(x). x will be handled as a vektor of qubtis.
 * @see quva.construct.QuvaConstruct
 * @see quva.construct.CalculateFunction
 * @see quva.core.QUBOMatrix.CustomPattern*/
public class CalculatePattern implements FunctionTransformation{
	/**Function to be used*/
	public QUBOMatrix.CustomPattern f;
	/**QuvaConstruct to be used*/
	public QuvaConstruct sc=null;
	/**This constructor takes the pattern to be processed. This will also create a Construct while applying the transformation.
	 * @param function function to be processed*/
	public CalculatePattern(QUBOMatrix.CustomPattern function) {
		QuvaDebug.logprnt("CalculatePattern.create","Creating...");
		f=function;
	}
	/**This constructor takes the pattern to be processed and the construct to be used
	 * @param function function to be processed
	 * @param sc construct to be used*/
	public CalculatePattern(QUBOMatrix.CustomPattern function,QuvaConstruct sc) {
		QuvaDebug.logprnt("CalculatePattern.create","Creating...");
		this.sc=sc;
		f=function;
	}
	/**{@inheritDoc}*/
	@Override
	@quva.core.Registers
	public void apply(QUBOMatrix m, String var, String target) {
		QuvaDebug.logprnt("CalculatePattern.apply","Applying on "+m.getClass().getSimpleName()+m.hashCode()+" "+var+" "+target);
		if(sc==null) sc=new SafeConstruct(var,m);
		new ImplementedFunction(m,sc,var,target).process(f);
		sc.process();
	}
}
