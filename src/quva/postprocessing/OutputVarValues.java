package quva.postprocessing;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import quva.core.QUBOMatrix;

/**A simple {@link PostProcessingHandler} that prints out the values of the variables. This can create outputs like:
 * <pre>
 * x: 0.0
 * y: 1.0
 * </pre>
 * When using a custom {@link OutputStyle} it can also make outputs like:
 * <pre>
 * The merchant visited city 2 first.
 * The merchant visited city 3 next.
 * The merchant visited city 1 last.
 * </pre>*/
public class OutputVarValues implements PostProcessingHandler {
	/**The {@code PrintStream} used by standard set to {@code System.out}*/
	public static PrintStream output=null;
	/**The OutputStyle used by the PostProcessingHandler*/
	public OutputStyle st=(var,value)->(var+": "+value+"\n");
	/**The variables that should be printed*/
	public String[] affectedVariables=null;
	/**Creates a PostProcessingHandler that prints out the values of the specified variables
	 * @param vars variables to be printed*/
	public OutputVarValues(String... vars) {
		affectedVariables=vars;
	}
	/**Creates a PostProcessingHandler that prints out the values of the specified variables by chaining the String given by the OutputStyle.
	 * @param vars variables to be printed
	 * @param st the {@link OutputStyle}*/
	public OutputVarValues(OutputStyle st,String... vars) {
		affectedVariables=vars;
		this.st=st;
	}
	/**Creates a PostProcessingHandler that prints out the values of all variables by chaining the String given by the OutputStyle.
	 * @param st the {@link OutputStyle}*/
	public OutputVarValues(OutputStyle st) {
		this.st=st;
	}
	/**Creates a PostProcessingHandler that prints out the values of all variables.*/
	public OutputVarValues() {
	}
	/**{@inheritDoc}*/
	@Override
	public void postprocessing(QUBOMatrix m, Map<String, Float> mp, int[] res) {
		Iterable<String> vars=(affectedVariables==null)?null:Arrays.asList(affectedVariables);
		if(affectedVariables==null) vars=mp.keySet();
		PrintStream newout=output;
		if(newout==null) newout=System.out;
		for(String s:vars) newout.print(st.generateLine(s, mp.get(s)));
	}
}
