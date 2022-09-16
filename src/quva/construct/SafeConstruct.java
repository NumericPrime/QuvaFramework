package quva.construct;

import java.util.Arrays;

import quva.core.QUBOMatrix;
import quva.core.QuvaDebug;

/**A construct based on safe carries. This is the easiest to use full implementation of the QuvaConstruct. It takes a variable and a matrix and sets the foundation for CalculateFunction and CalculatePattern. PowerSeries also uses this to create carries.
 * @see quva.construct.QuvaConstruct
 * @see quva.construct.CalculateFunction
 * @see quva.construct.CalculatePattern*/
public class SafeConstruct extends AbstractConstruct {
	/**Sets the standard rule carry rule. This rule works as follows:<br>
	 * Step 1: Look for the index of the first true entry (i)<br>
	 * Step 2: Create a duplicate of the first array (a) and an array that is all false (b)<br>
	 * Step 3: Set {@code a[i]=false} and {@code b[i]=true}<br><br>
	 * Example {@code [true,false,true]} -&gt; {@code [false,false,true]},{@code [false,false,true]}
	 * @see quva.construct.CarryRule*/
	public static CarryRule standardRule=(a)->{
		int firstTrue=0;
		loop: for(int i=(firstTrue=0);i<a.length;firstTrue=++i) if(a[i]) {
			QuvaDebug.log("SafeConstruct.process.firstTrue","Split at: "+firstTrue);
			break loop;}
		boolean[][] ret= {allFalse(a.length),a.clone()};
		ret[1][firstTrue]=false;
		ret[0][firstTrue]=true;

		return ret;
	};
	/**CarryRule used by the Construct*/
	public CarryRule cr;
	/**This construct takes the varname, a CarryRule and the matrix it gets applied to.
	 * @param var name of the variable
	 * @param cr CarryRule to be used
	 * @param mat QUBOMatrix it gets applied to*/
	public SafeConstruct(String var,CarryRule cr,QUBOMatrix mat) {
		super(var,mat);
		mainRegistry=new QuvaConstructRegister();
		this.cr=cr;
		int[] baseQubits=matrix.find(var);
		for(int i=1;i<1<<baseQubits.length;i++) 
		{
			boolean[] key=binary(baseQubits.length,i);
			int ind=0;
			//System.out.println(powerOf2(i));
			if((ind=powerOf2(i))>=0) mainRegistry.put(key, baseQubits[ind]);
			else mainRegistry.put(key, matrix.Qubits.next());
		}
	}
	/**This construct takes the varname, a CarryRule and the matrix it gets applied to. Also it copies the registry from another QuvaConstruct.
	 * @param var name of the variable
	 * @param cr CarryRule to be used
	 * @param mat QUBOMatrix it gets applied to
	 * @param qc Construct it copies the registry from*/
	public SafeConstruct(String var,CarryRule cr,QuvaConstruct qc,QUBOMatrix mat) {
		super(var,mat);
		mainRegistry=new QuvaConstructRegister();
		this.cr=cr;
		qc.process();
		mainRegistry=qc.getRegistry();
	}
	/**This construct takes the varname and the matrix it gets applied to. The standard CarryRule will be used.
	 * @param var name of the variable
	 * @param mat QUBOMatrix it gets applied to
	 * @see quva.construct.SafeConstruct#standardRule*/
	public SafeConstruct(String var,QUBOMatrix mat) {
		this(var,standardRule,mat);
	}
	@Override
	/**Creates the carries defined by the carry rule. It will iterate through each combination of the mainRegistry and will create a carry between the qubits represented by the boolean array given by the CarryRule.<br>
	 * Example:It will take e.g. {@code [true,false,true,false]} and apply the carry rule to split it up in <br> {@code [true,false,false,false]} and  {@code [false,false,true,false]}. Then it will search the registry for the associated Qubits and create carries accordingly.
	 * */
	public void process() {
		QuvaDebug.logprnt("SafeConstruct.process","Processing:","\n"+getClass().getSimpleName(),hashCode()+"\n",var+" ",matrix.getClass().getSimpleName(),matrix.hashCode()+"\n");

		for(boolean[] arr:mainRegistry.getKeys()) iteration:{
			validityCheck:{
			int al=0;
			for(int i=0;i<arr.length;i++) if(arr[i]) if(al++==1) break validityCheck;
			break iteration;
			}
			boolean[][] l=cr.assign(arr);
			QuvaDebug.log("SafeConstruct.process.carries",mainRegistry.get(arr),mainRegistry.get(l[0]),mainRegistry.get(l[1]));
			QuvaDebug.log("SafeConstruct.process.carries",QuvaDebug.booleanOutput(arr)+"\n\n"+QuvaDebug.booleanOutput(l[0])+"\n"+QuvaDebug.booleanOutput(l[1])+"\n---------\n");
						
			matrix.carry(
					mainRegistry.get(arr), 
					mainRegistry.get(l[0]), 
					mainRegistry.get(l[1]));}
	}
	public static boolean[] allFalse(int leng) {
		boolean[] ret=new boolean[leng];
		return ret;
	}
	/**{@inheritDoc}*/
	@Override
	public boolean authorizeRemoval(boolean[] b) {
		int counter=0;
		for(boolean bool:b) if(bool) counter++;
		counter-=b.length;
		if(counter==0) return true;
		if(counter<-1) return false;
		boolean[] alltrue=new boolean[b.length]; 
		for(int i=0;i<alltrue.length;i++) alltrue[i]=false;
		boolean[][] options=cr.assign(alltrue);

		boolean eq=true;
		for(int j=0;j<b.length&&eq;j++) eq=eq&&(b[j]==options[0][j]);
		if(eq) return false;
		eq=true;
		for(int j=0;j<b.length&&eq;j++) eq=eq&&(b[j]==options[1][j]);
		if(eq) return false;
		
		//if(counter==-1&&b[0]) return true;
		return true;
	}
	public static int  powerOf2(int p) {
		  int comp=1;
		  
		  for (int i=0;comp<=p;i++) {
		    if (comp==p) return i;
		    comp=comp<<1;
		  }
		  return -1;
		}
	public static boolean[] binary(int len,int numb) {
		boolean[] ret=new boolean[len];
		for(int i=0;i<len;i++) ret[i]=(numb>>i&1)==1;
		return ret;
	}
}
