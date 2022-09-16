package quva.construct;

import quva.core.QUBOMatrix;
import quva.core.QuvaDebug;
import quva.core.QuvaUtilities;
import quva.transform.*;

/**This takes a function and creates a variable containing the results of a pattern, which is a function that takes a vector of qubits and returns a float.
	 * @see quva.construct.QuvaConstruct
	 * @see quva.core.QUBOMatrix.CustomPattern
	 * @see quva.construct.FloatFunction*/
public class ImplementedFunction{
	/**Matrix from which the variables are taken*/
	public QUBOMatrix qbm;
	/**Construct used for creating the function*/
	public QuvaConstruct cs;
	/**Name of the variable the qubits of which are inserted in the pattern*/
	public String var1;
	/**Name of the new variable that saves the results*/
	public String varTarget;
	/**Here the weights are saved*/
	public QuvaConstructWeight qcw=new QuvaConstructWeight();
	/**Pattern to be implemented*/
	public QUBOMatrix.CustomPattern cp;
	/**This constructor takes all the data needed
	 * @param qbm the matrix all changes get applied to
	 * @param cs the construct that tells the class wich qubits to use
	 * @param var1 name of function this gets applied to
	 * @param varTarget name of the new variable
	 * @see quva.construct.QuvaConstruct
	 * @see quva.core.QUBOMatrix.CustomPattern*/
	public ImplementedFunction(QUBOMatrix qbm,QuvaConstruct cs,String var1,String varTarget){
		this.cs=cs;
		this.var1=var1;
		this.varTarget=varTarget;
		this.qbm=qbm;
	}
	/**This converts a FloatFunction to a CustomPattern
	 * @param f function to be used
	 * @param mat matrix t be used
	 * @param var variable that gets pluged into the function
	 * @see quva.construct.FloatFunction
	 * @see quva.core.QUBOMatrix.CustomPattern
	 * @return CustomPattern doing the same thing as the FloatFunction*/
	public static QUBOMatrix.CustomPattern convert(FloatFunction f,QUBOMatrix mat,String var){
		QuvaDebug.logprnt("ImplementedFunction.convert","Converting function...");
		
		return ((inp)->f.f(mat.inset(QuvaUtilities.toIntArray(inp),var)));
	}
	/**Processes a CustomPattern and creates a variable containing the result of the function.
	 * @param cp the CustomPattern to be used*/
	@quva.core.Registers
	public void process(QUBOMatrix.CustomPattern cp) {
		QuvaDebug.logprnt("ImplementedFunction.process","Used process on:","\n"+getClass().getSimpleName(),hashCode()+"\n");
		this.cp=cp;
		qcw.put(new boolean[qbm.find(var1).length],f(new boolean[qbm.find(var1).length]));
		QuvaConstructRegister qcr=cs.getRegistry();
		boolean[][] keys=qcr.getKeys();
		int[] asgqubits=qbm.find(var1);
		for(int i=0;i<asgqubits.length;i++) {
			boolean[] arr=new boolean[asgqubits.length];
			arr[i]=true;
			qcw.put(arr,f(arr)-f(new boolean[asgqubits.length]));
			if(QuvaDebug.checkTag("ImplementedFunction.process"))
				QuvaDebug.log("ImplementedFunction.process","Setting value: "+QuvaDebug.booleanOutput(arr)+" "+(f(arr)-f(new boolean[asgqubits.length])));
			
		}
		for(boolean[] bol:keys) get(bol);
		
		for(boolean[] bol:keys) {
			if(qcw.get(bol)==0&&cs.authorizeRemoval(bol)) {
				qcw.remove(bol);
				qcr.remove(bol);
			}
		}
		
		int index=0;
		int[] qubits=new int[keys.length];
		float[] we=new float[keys.length];
		for(boolean[] bol:keys) {
			we[index]=qcw.get(bol);
			qubits[index]=qcr.get(bol);
			index++;
		}
		qbm.register(varTarget,qubits,we);
	}
	/**Processes a FloatFunction and creates a variable containing the result of the function.
	 * @param f the FloatFunction to be used*/
	@quva.core.Registers
	public void process(FloatFunction f) {
		process(convert(f,qbm,var1));
	}
	public float f(boolean[] params) {
		int[] fw=qbm.find(var1);
		float[] vals=new float[fw.length];
		for(int i=0;i<params.length;i++) vals[i]=params[i]?1:0;
		return cp.f(vals);
	}
	public float get(boolean params[]) {
		if(qcw.containsKey(params)) return qcw.get(params);
		QuvaDebug.log("ImplementedFunction.process","Calculation: "+QuvaDebug.booleanOutput(params));
		int firstTrue=0;
		loop: for(int i=(firstTrue=0);i<params.length;firstTrue=++i) if(params[i]) {
			QuvaDebug.log("ImplementedFunction.process.firstTrue","Split at: "+firstTrue);
			break loop;
			}
		float ret=f(params);
		QuvaDebug.log("ImplementedFunction.process","Starting value "+ret);
		
		boolean rem1[]=params.clone();
		rem1[firstTrue]=false;
		ret-=f(rem1);
		QuvaDebug.log("ImplementedFunction.process","Subtracting "+f(rem1)+" (from "+QuvaDebug.booleanOutput(rem1)+")");
		for(int i=0;i<1<<params.length;i++) iteration: {
			boolean[] newparm=SafeConstruct.binary(params.length,i);
			if(!newparm[firstTrue]) break iteration;
			for(int j=0;j<params.length;j++) if(!params[j]&&newparm[j]) break iteration;
			boolean eq=true;
			for(int j=0;j<params.length;j++) eq=eq&&(params[j]==newparm[j]);
			if(eq) break iteration;
			if(QuvaDebug.checkTag("ImplementedFunction.process"))
			QuvaDebug.log("ImplementedFunction.process","Calling "+QuvaDebug.booleanOutput(newparm));

			ret-=get(newparm);
		}
		
		QuvaDebug.log("ImplementedFunction.process","Entry: "+ret);
		QuvaDebug.log("ImplementedFunction.process","----------------");
		
		qcw.put(params, ret);
		return ret;
	}
}
