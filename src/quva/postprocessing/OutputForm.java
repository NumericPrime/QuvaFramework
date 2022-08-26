package quva.postprocessing;

import java.io.PrintStream;
import java.util.Map;
import static quva.core.QuvaUtilities.*;
import quva.core.QUBOMatrix;
/**This {@code PostProcessingHandler} allows creating a custom form. Which will be printed in the post-processing.
 * <br> Using ?varname! will make the post-processing handler replace it with the value of the variable.<br>
 * If you use a number instead of a variablename like ?29! It will insert the value of the qubit with the index 30.
 * <br><b>Example:</b>
 * Assuming you get the value x=2 with q_0=1 from the quantumannealing you can use the form<br> The value of x is ?x!! The value of the first qubit is ?0!!<br>this will result in <br> The value of x is 2.0!The value of the first qubit is 1!*/
public class OutputForm implements PostProcessingHandler {
	/**The {@code PrintStream} used for outputting the postprocessing result.*/
	public PrintStream output=null;
	/**Thee form used.*/
	public String form="";
	/**Creates a new form
	 * @param form the form used*/
	public OutputForm(String form) {
		this.form=form;
	}
	/**Adds a {@code String} to the form.
	 * @param form {@code String} to be appended.*/
	public void append(String form) {
		this.form+=form;
	}
	/**{@inheritDoc}*/
	@Override
	public void postprocessing(QUBOMatrix m, Map<String, Float> mp, int[] res) {
		String inset=form;
		String involvedQubitValues[]=findStr("(?<=(\\?))\\d+(?=(\\!))",form);
		for(String var:involvedQubitValues) {
			int val=Integer.parseInt(var);
			if(res.length>val) inset=inset.replaceAll("\\?"+var+"\\!",""+res[val]);}
		String[] allInvolvedVars=findStr("(?<=(\\?))[^\\!\\?]+(?=(\\!))",inset);
		for(String var:allInvolvedVars) if(mp.containsKey(var)) inset=inset.replaceAll("\\?"+var+"\\!",""+ mp.get(var));
		if(output!=null) output.println(inset);
		else System.out.println(inset);
	}

}
