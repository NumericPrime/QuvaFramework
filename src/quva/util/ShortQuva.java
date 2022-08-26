package quva.util;
import static quva.core.QuvaUtilities.*;
import quva.core.QUBOMatrix;
/**An interpreter for a short form of quva intended for solving simple mathematical problems
 * @see quva.util.ShortQuva#put(String)*/
public class ShortQuva {
	/**The matrix being manipulated is saved here*/
	public QUBOMatrix quvaMatrix;
	/**Reads a {@code QUBOMatrix}
	 * @param matr {@code QUBOMatrix} to be edited*/
	public ShortQuva(QUBOMatrix matr) {
		quvaMatrix=matr;
	}
	/**Creates a empty {@code QUBOMatrix} with 100 qubits*/
	public ShortQuva() {
		quvaMatrix=new QUBOMatrix(100);
	}
	/**Creates a empty {@code QUBOMatrix} with a set amount of qubits
	 * @param numb number of qubits*/
	public ShortQuva(int numb) {
		quvaMatrix=new QUBOMatrix(numb);
	}
	/**Puts a string to be interpreted
	 * @param shortQuva string of commands to be read. There are the following rules:<br><ul>
	 * 	<li>Each command is seperated by a ;</li>
	 * 	<li>The register settings can be changed with ?*,_qubitcount_,_maxweight_<br>
	 * 	Using a negative maxWeight will instead of using a negative weight just set flip=true.</li>
	 * 	<li>You can register a natural number with ?_varname_ ,_qubitcount_<br>
	 * 	Using a negative maxWeight will instead of using a negative weight register an integer instead.</li>
	 * 	<li>You can register variables by using ?_varname_ to use the registerSettings and ?_varname_,_qubitcount_,_maxweight_ to use custom settings. (The sign of _maxweight_ determines whether to flip the first weight)</li>
	 * <li>You can use _var1_ , _var2_ -&gt; _var3_ to call multiply("var1","var2","var3"); </li>
	 * <li>You can use _var1_ , _var2_ -&gt;? _var3_ to call registerMultiplyCarries("var1","var2","var3"); </li>
	 * <li>You can create a linear equation by inserting a string like it is required in QUBOMatrix.linearEquation followed by =_floatNumber_ </li>
	 * <li>You can register a variable when using it the first time inserting the register command when using it the first time following by a !</li>
	 * </ul>
	 * <br>Examples:<br>
	 * <ul>
	 * <li>?*,3,-4 will call registerSettings(3,4,true);</li>
	 * <li>?*,3,4 will call registerSettings(3,4,false);</li>
	 * <li>?p will call register("p");</li>
	 * <li>?p,3,4 will call register("p",3,4,false);</li>
	 * <li>p,q-&gt;pq will call multiply("pq","p","q");</li>
	 * <li>?p,3,4! ,q-&gt;pq will be processed into ?p,3,4! ; p,q,-&gt;pq</li>
	 * <li>p,q-&gt;?pq will call registerMultiplyCarries("pq","p","q");</li>
	 * <li>p,q-&gt;?pq! will be processed into ?pq ; p,q-&gt;pq</li>
	 * <li>2+2*x=0 will call linearEquation("2+2*x-0");</li>
	 * <li>2*x=-2 will call linearEquation("2*x+2");</li>
	 * <li>y+x=2 will call linearEquation("x+y-2");</li>
	 * </ul>*/
	public void put(String shortQuva) {
		String[] strs=shortQuva.split(";");
		for(String s:strs) putCommand(s);
	}
	/**Interpretes a single command
	 * @param command command to be read*/
	public void putCommand(String command) {
		String str=command.replaceAll(" ", "");
		String[] inPlaceRegistered=findStr("\\?[^\\?]+?\\!",command);
		String[] nexVars=new String[inPlaceRegistered.length];
		for(int i=0;i<nexVars.length;i++) {nexVars[i]=putRegister(inPlaceRegistered[i].substring(0,inPlaceRegistered[i].length()-1));
		command=command.replace(inPlaceRegistered[i], nexVars[i]);
		}
		if(str.contains("->")) putMultiply(command);else
		if(str.contains("=")) putLinear(command);else
		if(str.startsWith("?")) putRegister(command);
	}
	/**Reads a multiply command (This method is not intended for normal use)
	 * @param command command to be read*/
	public void putMultiply(String command) {
		//System.out.println("mult " +command);
		String splittingString=command.replaceAll(" ", "");
		if(splittingString.contains("->?")) {
			splittingString=splittingString.replaceAll("\\-\\>\\?", ",");
			String retr[]=splittingString.split(",");
			quvaMatrix.registerMultiplyCarries(retr[2],retr[0],retr[1]);
		}else if(splittingString.contains("->")) {
			splittingString=splittingString.replaceAll("\\-\\>", ",");
			String retr[]=splittingString.split(",");
			quvaMatrix.multiply(retr[2],retr[0],retr[1]);			
		}
	}
	/**Reads a register command (Not Intended for normal use)
	 * @param command command to be read
	 * @return name of the registered variable*/
	public String putRegister(String command) {
		//System.out.println("register " +command);
		String str=command.replaceAll(" ", "");
		str=str.substring(1,str.length());
		String reg[]=str.split(",");
		if(!reg[0].equals("*")) {
		if(reg.length>2) quvaMatrix.register(reg[0],Integer.parseInt(reg[1]),Math.abs(Float.parseFloat(reg[2])),Float.parseFloat(reg[2])<0);
		if(reg.length==2) quvaMatrix.register(reg[0],Integer.parseInt(reg[1]),1<<Math.abs(Integer.parseInt(reg[1])-1),Float.parseFloat(reg[1])<0);
		if(reg.length==1) quvaMatrix.register(reg[0]);
		}
		else {
			quvaMatrix.registerSettings(Integer.parseInt(reg[1]),Math.abs(Float.parseFloat(reg[2])),Float.parseFloat(reg[2])<0);}
		return reg[0];
	}
	/**Reads a linear equation (Not Intended for normal use)
	 * @param command command to be read*/
	public void putLinear(String command) {
		//System.out.println("linear " +command);
		String[] strs=command.split("=");
		float rh=Float.parseFloat(strs[1]);
		String assoc="-"+rh;
		if(rh<0) assoc="+"+rh;
		quvaMatrix.linearEquation(strs[0]+assoc);
	}
}
