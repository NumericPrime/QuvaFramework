package quva.core;

import java.util.*;

import quva.transform.PowerSeries;
import quva.util.QuvaModifiers;
import quva.postprocessing.BundledHandler;
import quva.postprocessing.PostProcessingHandler;

import java.lang.reflect.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**The main class of Quva. Nearly everything you do using quva involves manipulating a {@code QUBOMatrix} object. For creating applications using {@link QuvaApplication} is recommended.
 * @see quva.core.QuvaApplication*/
public class QUBOMatrix extends ExecuteProgram implements Cloneable,Serializable{
	private static final long serialVersionUID = 5339969823213182075L;
	/**The last {@code QUBOMatrix} created is stored here*/
	public static transient QUBOMatrix lastMatrix=null;
	public float chainStrength=1;
	/**Sets the new chain-strength
	 * @param newStrength new chain-strength to be used*/
	public void setChainStrength(float newStrength) {
		chainStrength=newStrength;
	}
	/**Applies an transformation
	 * @param tr Transformation to be applied
	 * @param var1 first variable
	 * @param var2 second variable (If the transformation only has one src this will be ignored)
	 * @param target third variable (If the transformation doesn't have a target this will be ignored)*/
	public void applyAbstractTransformation(quva.transform.AbstractTransform tr,String var1,String var2,String target) {
		tr.apply(this,var1);
		tr.apply(this,var1,target);
		tr.apply(this,var1,var2,target);
	}
	/**Applies a transformation
	 * @param tr Transformation to be applied
	 * @param var1 first variable
	 * @param var2 second variable
	 * @param target third variable*/
	public void applyTransformation(quva.transform.MapTransformation tr,String var1,String var2,String target) {
		applyAbstractTransformation(tr,var1,var2,target);
	}
	/**Applies a function transformation
	 * @param tr Transformation to be applied
	 * @param var1 first variable
	 * @param target second variable*/
	public void applyTransformation(quva.transform.FunctionTransformation tr,String var1,String target) {
		applyAbstractTransformation(tr,var1,null,target);
	}
	/**Applies a single variable transformation
	 * @param tr Transformation to be applied
	 * @param var variable*/
	public void applyTransformation(quva.transform.SingleVarTransformation tr,String var) {
		applyAbstractTransformation(tr,var,null,null);
	}
	/**removes a qubit however doesn't make the matrix smaller.
	 * @param a qubit to be removed
	 * @param as_one determines whether the qubit is supposed to be set to one
	 * @see quva.core.QUBOMatrix#remove(int)
	 * @see quva.core.QUBOMatrix#remove(int,boolean)*/
	public void removeInPlace(int a,boolean as_one) {
		if(as_one) for(int i=0;i<matrix.length;i++) matrix[i][i]+=get(a,i);
		for(int i=0;i<matrix.length;i++) matrix[Math.min(a, i)][Math.max(a, i)]=0;
	}
	/**removes a qubit however doesn't make the matrix smaller.
	 * @param a qubit to be removed
	 * @see quva.core.QUBOMatrix#remove(int)
	 * @see quva.core.QUBOMatrix#remove(int,boolean)*/
	public void removeInPlace(int a) {
		removeInPlace(a,false);
		}
	/**Gets the value of the coupling between two qubits
	 * @param i value of the first qubit
	 * @param j value of the second qubit
	 * @return corresponding entry in the matrix*/
	public float get(int i,int j) {
		return matrix[Math.min(i, j)][Math.max(i, j)];
	}
	/**Registers a natural number (integer&gt;0) with given number of qubits
	 * @param name name of the variable
	 * @param l number of qubits involved
	 * @see quva.core.QUBOMatrix#registerInt(String,int)*/
	@Registers
	public void registerNat(String name,int l) {
		register(name,l,1<<(l-1));
	}
	/**Registers a integer with given number of qubits (can be &lt;0)
	 * @param name name of the variable
	 * @param l number of qubits involved
	 * @see quva.core.QUBOMatrix#registerNat(String,int)*/
	@Registers
	public void registerInt(String name,int l) {
		register(name,l,1<<(l-1),true);
	}
	/**This number is the "payoff" for choosing all elements in an unsafe carry*/
	float unsafeCarryOffset=0.01f;
	/**This constructs an unsafe carry that can join any amount qubits meaning:<br>
	 * {@code q_target=q_elements[0]*q_elements[1]*...*q_elements[n]}
	 * @param target target qubits
	 * @param elements qubits to be joined*/
	@UnsafeAction
	public void unsafeCarry(int target,int... elements) {
		float mainWeight=elements.length-unsafeCarryOffset;
		for(int qubit:elements) add(-1f,target,qubit);
		add(mainWeight,target);
	}
	/**This class provides suggestions and ways to optimize your HamiltonMatrix*/
	public static class Optimizer{
		/**All qubits sugested by suggest and suggestRemoval are saved here*/
		public static LinkedList<Integer> removal=new LinkedList<Integer>();
		/**Here the currently optimized matrix is saved*/
		public static QUBOMatrix mat;
		/**Suggests qubits to be removed
		 * @param m matrix to be analysed*/
		public static void suggest(QUBOMatrix m){
			removal.clear();
			mat=m;
			for(int i=0;i<m.matrix.length;i++) {
				float sum=0;
				for(int j=0;j<m.matrix.length;j++) {
				float fetch=m.get(i,j);
				if(fetch<0) sum-=fetch;
			}
			if(sum<=m.get(i, i)) suggestRemoval(i);
			}
		}
		/**Suggests the removal of one qubit and all associated carries
		 * @param i qubit to be removed*/
		public static void suggestRemoval(int i) {
			System.out.println("qubit "+i+" should be removed");
			removal.add(i);
			for(int j=0;j<mat.assignedCarries.size();j++) if(mat.assignedCarriesN1.get(j)==i||mat.assignedCarriesN2.get(j)==i) {
				suggestRemoval(mat.assignedCarries.get(j));
				
			}
		}
		/**Optimizes your problem by removing unneccessary qubits
		 * @param m matrix to be optimized*/
		public static void optimize(QUBOMatrix m){
			mat=m;
			for(int i=0;i<m.matrix.length;i++) {
				float sum=0;
				for(int j=0;j<m.matrix.length;j++) {
				float fetch=m.get(i,j);
				if(fetch<0) sum-=fetch;
			}
			if(sum<=m.get(i, i)) optimizeRemoval(i);
			}
		}
		/**Removes a single qubit
		 * @param i qubit to be removed*/
		public static void optimizeRemoval(int i) {
			mat.removeInPlace(i,false);
			removal.add(i);
			for(int j=0;j<mat.assignedCarriesMode.size();j++) 
				if(mat.assignedCarriesMode.get(j)>0)if(mat.assignedCarriesN1.get(j)==i||mat.assignedCarriesN2.get(j)==i) {
				suggestRemoval(mat.assignedCarries.get(j));
			mat.set(1,mat.assignedCarriesN1.get(j),mat.assignedCarriesN2.get(j));	
			//System.out.println("remove "+mat.assignedCarriesN1.get(j)+" "+mat.assignedCarriesN2.get(j));
			}

			for(int j=mat.assignedCarriesMode.size()-1;j>=0;j--) 
				if(mat.assignedCarriesMode.get(j)>0)if(mat.assignedCarries.get(j)==i) {
			mat.set(1,mat.assignedCarriesN1.get(j),mat.assignedCarriesN2.get(j));	
			//System.out.println("remove "+mat.assignedCarriesN1.get(j)+" "+mat.assignedCarriesN2.get(j));
			mat.assignedCarriesMode.remove(j);
			mat.assignedCarriesN1.remove(j);
			mat.assignedCarriesN2.remove(j);
			mat.assignedCarries.remove(j);
				}
		
		}
		/**Removes a single qubit by setting it to one. DOESN'T work yet
		 * @param i qubit to be removed*/
		public static void optimizeRemovalAsOne(int i) {
			mat.removeInPlace(i,true);
			removal.add(i);
			for(int j=0;j<mat.assignedCarries.size();j++) if(mat.assignedCarriesN1.get(j)==i||mat.assignedCarriesN2.get(j)==i) {
				suggestRemoval(mat.assignedCarries.get(j));
			}
		}
	}
	/**Removes one of two qubits by setting them to a equal value
	 * @param i qubit to be preserved
	 * @param j qubit to be overriden*/
	public void fuse(int i,int j) {
		for(int k=0;k<matrix.length;k++) {
			add(get(k,j),k,i);
		}
		add(get(i,j),i,i);
		removeInPlace(j,false);
	}
	public interface  CustomPattern {
		  float f(float[] inp);
		}
	/**In this HashMap the weights of registered variables are saved*/
  public Map<int[], float[]> registeredWeights=new HashMap<int[],float[]>();
  	/**In this HashMap the qubits of registered variables are saved*/
  public Map<String, int[]> registeredVars=new HashMap<String, int[]>();
  /**In this HashMap the PowerSeries of registered variables are saved*/
  public Map<String, PowerSeries> registeredPower=new HashMap<String, PowerSeries>();
  /**In this ArrayList all carries created by link and carry are saved.*/
  public List<Integer> assignedCarries=new ArrayList<Integer>();
  /**In this ArrayList all q1's of link/carry are saved.*/
  public List<Integer> assignedCarriesN1=new ArrayList<Integer>();
  /**In this ArrayList all q2's of link/carry are saved.*/
  public List<Integer> assignedCarriesN2=new ArrayList<Integer>();
  /**In this ArrayList info about the way the carry was created is saved (link,linkUnregistered,iLink)*/
  public List<Integer> assignedCarriesMode=new ArrayList<Integer>();
  /**Finds a carry between v1 and v2 if ilink isn't used the order doesn't matter. Meaning that you can search for a carry between 3 and 5 as well as 5 as 3.
   * @param v1 first qubit involved
   * @param v2 second qubit involved
   * @return the qubit that holds the carry (if there isn't one it returns -1)*/
  public int findCarry(int v1,int v2) {
	  for(int i=0;i<assignedCarriesMode.size();i++) {
		  if(assignedCarriesN1.get(i)==v1)
			  if(assignedCarriesN2.get(i)==v2) return assignedCarries.get(i);
		  if(assignedCarriesMode.get(i)!=0)
		  if(assignedCarriesN1.get(i)==v2)
			  if(assignedCarriesN2.get(i)==v1) return assignedCarries.get(i);
	  }
	  return -1;
  }
  /**Checks all combinations between qubitsI and qubitsJ for combinations that satisfy the rule rl and add the value specified by the addition rule
   * @param qubitsI first set of qubits
   * @param qubitsJ second set of qubits
   * @param rl rule to determine for what qubits to apply the AdditionRule
   * @param arl specification what to do with qubits satisfying the rule
   * @see quva.core.QUBOMatrix#applyRule(int[], Rule1Bit, AdditionRule1Bit)*/
  public void applyRule(int qubitsI[],int qubitsJ[],Rule2Bits rl,AdditionRule2Bits arl) {
	  for(int i:qubitsI) for(int j:qubitsJ)if(rl.apply(i, j)) add(arl.add(i,j),i,j);
  }
  /**Checks qubits for combinations that satisfy the rule rl and add the value specified by the addition rule
   * @param qubits set of qubits
   * @param rl rule to determine for what qubits to apply the AdditionRule
   * @param arl specification what to do with qubits satisfying the rule
   * @see quva.core.QUBOMatrix#applyRule(int[], int[], Rule2Bits, AdditionRule2Bits)*/
  public void applyRule(int qubits[],Rule1Bit rl,AdditionRule1Bit arl) {
	  for(int i:qubits) if(rl.apply(i)) add(arl.add(i),i);
  }
  /**This method allows generating the weights for a binary number.<br>Calls {@code genWeights(start,n,false)}.
   * @param start the value of the <b>highest</b> weight
   * @param n number of weights to be generated
   * @return the generated weights
   * @see quva.core.QUBOMatrix#genWeights(float,int,boolean)*/
  public static float[] genWeights(float start, int n) {
    return genWeights(start, n, false);
  }
  /**This method allows generating the weights for a binary number.<br>The array will look like this:<br>
   * {@code start,start/2,start/4,...,start/2^n}.<br>This can be used to implement positive rational numbers.<br>
   * You may also flip the sign of the first weight also allowing you to use negative numbers.
   * @param start the value of the <b>highest</b> weight
   * @param n number of weights to be generated
   * @param flip determines whether the first qubit is to be flipped
   * @return the generated weights*/
  public static float[] genWeights(float start, int n, boolean flip) {
    float[] ret=new float[n];
    for (int i=0; i<ret.length; i++) ret[i]=start/(float)(1<<i);
    if (flip) ret[0]*=-1;
    return ret;
  }
  /**The standard {@code QubitManager}.*/
  public QubitManager Qubits=new QubitManager();
  /** A {@code QubitManager} keeps track of all unassigned qubits. You may also create a custom one and have it run parallel to {@code Qubits} if you wish so.*/
  public class QubitManager {
	  public int lastSingle;
	/**Saves the last array given*/
	  public int last[];
    /**Gives a array of l unassigned qubits. These will be deleted from the unassigned list. The result is also saved in last. You can use the get method to get the result of this method again.
     * @param l number of qubits to be assigned 
     * @return an array of unused Qubits 
     * @see quva.core.QUBOMatrix.QubitManager#next()
     * @see quva.core.QUBOMatrix.QubitManager#get()*/
    public int[] next(int l) {
      int[] ret=new int[l];
      for (int i=0; i<l; i++) ret[i]=unassigned.pop();
      last=ret;
      return ret;
    }
    /**Returns the next unassigned qubit
     * @return one unused qubit
     * @see quva.core.QUBOMatrix.QubitManager#next(int)
     * @see quva.core.QUBOMatrix.QubitManager#getInt()*/
    public int next() {
    	return lastSingle=unassigned.pop();
    }
    /**Gets the last result given by {@code next(int)}
     * @return the last array of unused qubits returned
     * @see quva.core.QUBOMatrix.QubitManager#next()*/
    public int[] get() {
      return last;
    }
    /**Gets the last result given by {@code next()}
     * @return the last single unassigned qubit return
     * @see quva.core.QUBOMatrix.QubitManager#next()*/
    public int getInt() {
      return lastSingle;
    }
    /**Replaces the unassigned qubits with the ones in the array.
     * @param qubits the qubits to form the unassigned Stack*/
    public void unassign(int[] qubits) {
      unassigned.removeAllElements();
      for (int i=qubits.length-1; i>=0; i--) unassigned.push(qubits[i]);
    }
    /**Here the unassigned qubits are saved*/
    public Stack<Integer> unassigned=new Stack<Integer>();
    /**Finds all unassigned qubits. An unassigned qubit must fit 2 criteria:<br>
     * 1. There is no bias or interaction with other qubits given<br>
     * 2. It isn't registered as part of a variable*/
    public void findUnassigned() {
      unassigned.removeAllElements();
      HashMap<Integer, String> assigned=new HashMap<Integer, String>();
      for (int e : assignedCarries) if (assigned.get(e)==null) {
        assigned.put(e, "carry");
      }
      for (String varName : registeredVars.keySet()) {
        for (int e : registeredVars.get(varName)) if (assigned.get(e)==null) {
          assigned.put(e, varName);
        }
      }
      for (int bit=matrix.length-1; bit>=0; bit--) if (assigned.get(bit)==null) {
        boolean valid=true;
        for (int i=0; i<matrix.length&&valid; i++) {
          valid=matrix[Math.min(bit, i)][Math.max(bit, i)]==0;
        }
        if (valid) unassigned.push(bit);
      }
    }
  }
  /**A simple diagnosis function. This function searches for problematic variable names and qubits wich are involved in different variables or are carries and involved in a variable.<br>
   * It will not recognize carries generated in {@code multiplyCarries} as carries rather as part of a variable if they get registered as one.
   * @deprecated This method is currently being reworked*/
  @Deprecated
  public void findProblems() {
    System.out.println("---------------------------Ambiguous qubits-------------------------------------------------");
    HashMap<Integer, String> assigned=new HashMap<Integer, String>();
    for (int i=0;i<assignedCarries.size();i++) if (assigned.get(assignedCarries.get(i))==null) {
      if(assignedCarriesMode.get(i)<2) assigned.put(assignedCarries.get(i), "carry");
    } else if(assignedCarriesMode.get(i)<2)  System.out.println("The qubit with the number "+assignedCarries.get(i)+" has been assigned as carry twice!");
    for (String varName : registeredVars.keySet()) {
      for (int e : registeredVars.get(varName)) if (assigned.get(e)==null) {
        assigned.put(e, varName);
      } else
        System.out.println("The qubit with the number "+e+" has been assigned as a part of "+varName+ " as well as "+assigned.get(e)+"!");
    }
    System.out.println("---------------------------Discouraged variablenames----------------------------------------");
    for (String varName : registeredVars.keySet()) {
      if (varName.contains(" "))
        System.out.println(varName+": Using spaces in variable names is discouraged as it will not work with linearEquation");
      String discouragedChars[]={"*", "-", "+"};
      for (String ch : discouragedChars) if (varName.contains(ch))
        System.out.println(varName+": Using "+ch+" in variable names is discouraged as it will not work with linearEquation");
    }
  }
  /**Inserts a carry that will make {@code q_target=q_v1*q_v2}
   * @param target number of the target qubit
   * @param v1 first qubit
   * @param v2 second qubit
   * @see quva.core.QUBOMatrix#link(int,int,int)*/
  public void carry(int target, int v1, int v2) {
    link(target, v1, v2);
  }
  public int registerSettingQubit= 3;
  public float registerSettingWeight= 3;
  public boolean registerSettingFlip=true;
  /**Sets the standard settings for register. These will be applied as the lacking argument for {@code register(String...)}
   * @param qubitCount number of qubits to be registered
   * @param weight value of the highest weight
   * @param flip determines whether the highest weight should be flipped
   * @see quva.core.QUBOMatrix#register(String...)
   * */
  @Registers
  public void registerSettings(int qubitCount,float weight,boolean flip) {
	  registerSettingQubit=qubitCount;
	  registerSettingWeight=weight;
	  registerSettingFlip=flip;
  }
  /**Registers a list of var names with the settings specified in {@code registerSettings}
   * @param vars variables to be registered
   * @see quva.core.QUBOMatrix#registerSettings(int,float,boolean)*/
	@quva.core.Registers
  public void register(String... vars) {
	  for(String var:vars) register(var,registerSettingQubit,registerSettingWeight,registerSettingFlip);
  }
  /**Associates a list of qubits as with a set of weights<br>Using this is not recommended as neither {@code findUnassigned} not {@code findProblems} will recognize them
   * @param qubits involved qubits
   * @param weights involved weights
   * @see quva.core.QUBOMatrix#register(String,int[],float[])*/
	@quva.core.Registers
  public void register(int[] qubits, float[] weights) {
    registeredWeights.put(qubits, weights);
  }
  /**Associates a {@code String} with a list of qubits
   * @param varname name of the variable
   * @param qubits involved qubits
   * @see quva.core.QUBOMatrix#register(String,int[],float[])*/
	@quva.core.Registers
  public void register(String varname, int[] qubits) {
    registeredVars.put(varname, qubits);
  }
  /**Associates a {@code String} with a list of qubits as well as weights generated by {@code genWeight}
   * @param varname name of the variable
   * @param qubits qubits involved
   * @param start value of the highest weight
   * @param flip determines whether the sign of the first weight should be flipped
   * @see  quva.core.QUBOMatrix#register(String,int[],float[])
   * @see  quva.core.QUBOMatrix#genWeights(float, int, boolean)*/
	@quva.core.Registers
  public void register(String varname, int[] qubits,float start,boolean flip) {
    register(varname, qubits,genWeights(start,qubits.length,flip));
  }
  /**Associates a {@code String} with a list of qubits as well as weights generated by {@code genWeight}. flip is set to false
   * @param varname name of the variable
   * @param qubits qubits involved
   * @param start value of the highest weight
   * @see quva.core.QUBOMatrix#register(String,int[],float,boolean)
   * @see quva.core.QUBOMatrix#register(String,int[],float[])
   * @see  quva.core.QUBOMatrix#genWeights(float, int)*/
	@quva.core.Registers
  public void register(String varname, int[] qubits,float start) {
    register(varname, qubits,genWeights(start,qubits.length));
  }
  /**Associates a {@code String} with a list of qubits generated by {@code Qubits.next} and with weights generated by {@code genWeight}
   * @param varname name of the variable
   * @param qubitsN number of qubits
   * @param start value of the highest weight
   * @param flip determines whether the first weight should be flipped
   * @see quva.core.QUBOMatrix#register(String,int[],float[])
   * @see  quva.core.QUBOMatrix#genWeights(float, int, boolean)*/
	@quva.core.Registers
  public void register(String varname, int qubitsN,float start,boolean flip) {
    register(varname, Qubits.next(qubitsN),genWeights(start,qubitsN,flip));
  }
  /**Associates a {@code String} with a list of qubits generated by {@code Qubits.next} and with weights generated by {@code genWeight}
   * @param varname name of the variable
   * @param qubitsN number of qubits
   * @param start value of the highest weight
   * @see quva.core.QUBOMatrix#register(String,int,float,boolean)
   * @see quva.core.QUBOMatrix#register(String,int[],float[])
   * @see  quva.core.QUBOMatrix#genWeights(float, int)*/
	@quva.core.Registers
  public void register(String varname, int qubitsN,float start) {
    register(varname, Qubits.next(qubitsN),genWeights(start,qubitsN));
  }
  /**Associates a {@code String} with a list of qubits generated by {@code Qubits.next}
   * @param varname name of the variable
   * @param qubitCount number of qubits
   * @see quva.core.QUBOMatrix#register(String,int[],float[])*/
	@quva.core.Registers
  public void register(String varname, int qubitCount) {
    register(varname, Qubits.next(qubitCount));
  }
  /**Associates a {@code String} with a list of qubits generated by {@code Qubits.next} and weights
   * @param varname name of the variable
   * @param qubitCount number of qubits
   * @param weights weights given
   * @see quva.core.QUBOMatrix#register(String,int,float,boolean)
   * @see quva.core.QUBOMatrix#register(String,int[],float[])*/
	@quva.core.Registers
  public void register(String varname, int qubitCount, float[] weights) {
    register(varname, Qubits.next(qubitCount),weights);
  }
  /**Associates a {@code String} with a list of qubits and a list of weights
   * @param varname name of the variable
   * @param qubits array of qubits
   * @param weights weights given
   * @see quva.core.QUBOMatrix#register(String,int,float,boolean)*/
	@quva.core.Registers
  public void register(String varname, int[] qubits, float[] weights) {
    registeredWeights.put(qubits, weights);
    registeredVars.put(varname, qubits);
  }
  /**Finds the qubtis defining a variable
   * @param varname name of the variable
   * @throws VarNotRegisteredException when trying to call a non-existent variable
   * @return an array of qubits where all the involved qubits are saved
   * @see quva.core.VarNotRegisteredException*/
  public int[] find(String varname) {
	if(registeredVars.get(varname)==null) throw new VarNotRegisteredException("The variable "+varname+" has not been registered");
    return registeredVars.get(varname);
  }
  /**Finds the weights associated with a list of qubtis
   * @param qubits list of qubtis
   * @throws VarNotRegisteredException when trying to call a non-existent variable
   * @return the weights involved in a variable
   * @see quva.core.VarNotRegisteredException*/
  public float[] findWeight(int[] qubits) {
		if(registeredWeights.get(qubits)==null) throw new VarNotRegisteredException("The variable used has not been registered");
    return registeredWeights.get(qubits);
  }
  /**Finds the weights associated with a variable name
   * @param varname name of the variable
   * @throws VarNotRegisteredException when trying to call a non-existent variable
   * @return the weights involved in a variable
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#findWeight(int[])*/
  public float[] findWeight(String varname) {
    return findWeight(find(varname));
  }
  /**Writes the weights for a potential multiplication
   * @param var1 first variable
   * @param var2 second variable
   * @throws VarNotRegisteredException when trying to use a non-existent variable
   * @return the weights recommended for a variable saving the product of var1 and var2
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#registerMultiply(String, String, String)
   * @see quva.core.QUBOMatrix#multiply(String, String, String)*/
  public float[] multiplyRecommendation(String var1,String var2) {
	  int retL=0;
	  float retMax=0;
	  boolean retFlip=false;
	  float[] wvar1=findWeight(var1);
	  float[] wvar2=findWeight(var1);
	  retFlip=wvar1[0]<0||wvar2[0]<0;
	  retL=wvar1.length+wvar2.length;
	  float corwe1=wvar1[0]>0?wvar1[0]*2:wvar1[0];
	  float corwe2=wvar2[0]>0?wvar2[0]*2:wvar2[0];
	  retMax=corwe1*corwe2/(!retFlip?2:1);
	  return genWeights(retMax,retL,retFlip);
  }
  /**Multiplies two numbers and creates a new variable where the results are saved<br>
   * this will create a new variable with minimal qubitcount. Not to be confused with {@code registerMultiplyCarries}
   * @param target variable the result gets saved in
   * @param var1 first variable
   * @param var2 second variable
   * @throws VarNotRegisteredException when trying to use a non-existent variable
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#multiplyRecommendation(String, String)
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String, String, String)
   * @see quva.core.QUBOMatrix#multiply(String, String, String)*/
	@quva.core.Registers
  public void registerMultiply(String target,String var1,String var2) {
	  float we[]= multiplyRecommendation(var1,var2);
	  register(target,Qubits.next(we.length),we);
	  multiply(target,var1,var2);
  }
  /**Adds the following condition:<br>
   * The variable with the qubtis in targ now is the product of the variable in the qubits in n1 and n2.
   * @param targ target where the result of the multiplication is saved
   * @param n1 qubits of the first factor
   * @param n2 qubits of the second factor
   * @param carries used carries
   * @throws VarNotRegisteredException when no weights are associated with the qubits used
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String, int[],int[], int[])*/
  public void multiply(int[] targ, int[] n1, int[] n2, int[]...carries) {
    if (findWeight(targ)==null) throw new VarNotRegisteredException("The weights assigned to the target couldn't be found");
    if (findWeight(n1)==null) throw new VarNotRegisteredException("The weights assigned to the first factor couldn't be found");
    if (findWeight(n2)==null) throw new VarNotRegisteredException("The weights assigned to the second factor couldn't be found");
    multiply(targ, findWeight(targ), n1, findWeight(n1), n2, findWeight(n2), carries);
  }
  /**Sets the variable associated with targ to the product of the two other variables
   * @param targ name of the variable of that gets set to the product
   * @param n1 name of the first factor
   * @param n2 name of the second factor
   * @param carries carries given
   * @throws VarNotRegisteredException when no weights are associated with the qubits used
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String, String,String, int[])*/
  public void multiply(String targ, String n1, String n2, int[]...carries) {
    if (find(targ)==null) throw new VarNotRegisteredException("The variable "+targ+" hasn't been registered");
    if (find(n1)==null) throw new VarNotRegisteredException("The variable "+n1+" hasn't been registered");
    if (find(n2)==null)throw new VarNotRegisteredException("The variable "+n2+" hasn't been registered");
    multiply(find(targ), find(n1), find(n2), carries);
  }
  /**Sets the variable associated with targ to the product of the two other variables
   * @param targ name of the variable of that gets set to the product. This time the carries are chosen by Qubits.next
   * @param n1 name of the first factor
   * @param n2 name of the second factor
   * @throws VarNotRegisteredException when no weights are associated with the qubits used
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String, String,String)*/
  public void multiply(String targ, String n1, String n2) {
    int qubitNumb=find(n1).length*find(n2).length;
    multiply(targ, n1, n2, Qubits.next(qubitNumb));
  }
  /**Multiplies a {@code float} array with a number
   * @param factor factor to be applied
   * @param arr array to wich the factor gets added
   * @return the array after being multiplied by the factor*/
  public float[] multArray(float factor, float[] arr) {
    float[] ret=new float[arr.length];
    for (int i=0; i<ret.length; i++) ret[i]=arr[i]*factor;
    return ret;
  }
  /**Inserts a linear equation as a condition:<br>
   * The equation must use the varnames already defined. In each part of the sum there must by a factor followed by a variable separated by a *. An example would be 2*x. Alternatively you can enter single numbers as part of a sum to make them a constant.
   * <br>A valid example would be the equation: {@code "-4+2*x"}<br>This will create the condition "-4+2x=0"<br>If the factor is 1 you can leave the 1* out.
   * @param equation linear expression being set to 0
   * @throws VarNotRegisteredException when trying to use a non-existent variable*/
  public void linearEquation(String equation) {
	equation="+"+equation;
	equation=equation.replaceAll("\\+\\-", "-");
	equation=equation.replaceAll("\\+\\+", "+");
    equation=equation.replaceAll(" ", "");
    equation=equation.replaceAll("\\-\\-", "+");
    equation=equation.replaceAll("\\+\\-", "-");
    equation=equation.replaceAll("\\-\\+", "-");
    equation=equation.replaceAll("\\+", " +");
    equation=equation.replaceAll("\\-", " -");
    String sumParts[] =equation.split(" ");
    for(int i=0;i<sumParts.length;i++) {
    	if(!sumParts[i].matches("(\\+|\\-)*?\\d+\\.??\\d*?")) if(sumParts[i].split("\\*").length==1){
    		sumParts[i]=sumParts[i].replace("+", "+1*").replace("-", "-1*");
    	}
    }
    List<String> vars=new ArrayList<String>();
    List<Float> fact=new ArrayList<Float>();
    float sumPart=0;
    for (String s : sumParts) {
      String splitted[]=s.split("\\*");
      if (splitted.length>1) {
        fact.add(Float.parseFloat(splitted[0]));
        vars.add(splitted[1]);
      } else if (splitted[0].length()>0) {
        sumPart+=Float.parseFloat(splitted[0]);
      }
    }
    int[][] varsArr=new int[vars.size()][1];
    float[][] factArr=new float[vars.size()][1];
    for (int i=0; i<vars.size(); i++) {
      varsArr[i]=(find(vars.get(i)));
      factArr[i]=(multArray(fact.get(i), findWeight(vars.get(i))));
    }
    addQuadratic(sumPart, wrapArr(factArr), wrapArr(varsArr));
  }
  /**Implements polynomial equations. The rules are the same as with {@link #linearEquation(String)} however only one variable is allowed. You may however add powers using ^n with n being a integer. When a PowerSeries has been registered for a variable, it will be used if not a new PowerSeries will be created.
   * @param equation the polynomial equation itself
   * @param additional a linear equation that will be attached to the polynomial equation. It will be attached directly to the string so you should use something like +x
   * @see #polynomialEquation(String,PowerSeries, String)*/
  public void polynomialEquation(String equation,String additional) {
	  String alteredEquation=("0+"+equation)
			  .replaceAll(" ", "")
			  .replaceAll("\\+\\+", "+")
			  .replaceAll("\\-\\+", "-")
			  .replaceAll("\\+\\-", "-")
			  .replaceAll("\\-\\-", "+")
			  .replaceAll("\\-", "+-")
			  .replaceAll(" ", "");
	  String[] sumParts=alteredEquation.split("\\+");
	  String[] coefficients=findStr("(?<!\\+\\-\\*)[^\\+\\-\\*]+?(?=\\^)",alteredEquation);
	  if(coefficients.length==0) throw new EquationFormatException("No valid coefficients have been found in the equation "+equation);
	  String var=coefficients[0];
	  for(String vars:coefficients) if(!vars.matches(var)) throw new EquationFormatException("There has been an invalid variablename fist variable: "+var+" invalid variable: "+vars);
	  int maxexp=1;
	  for(String part:sumParts) {
		  if(part.contains(var)) {
			int exp=1;
		  	String sugestions[]=findStr("\\^\\d+",part);
		  	if(sugestions.length>=1) exp=Integer.parseInt(sugestions[0].substring(1));
		  	if(exp>maxexp) maxexp=exp;
		  }
	  }
	  if(registeredPower.containsKey(var)) 
		  polynomialEquation(equation,registeredPower.get(var),additional);
	  else {
	  PowerSeries s=new PowerSeries(maxexp,"poly<"+var+"><"+maxexp+">");
	  applyTransformation(s,var);
	  registeredPower.put(var, s);
	  polynomialEquation(equation,s,additional);}
  }
  /**Implements polynomial equations. The rules are the same as with {@link #linearEquation(String)} however only one variable is allowed. You may however add powers using ^n with n being a integer.
   * @param equation the polynomial equation itself
   * @param s the attached (and already applied {@link quva.transform.PowerSeries})
   * @param additional a linear equation that will be attached to the polynomial equation. It will be attached directly to the string so you should use something like +x
   * @see #polynomialEquation(String, String)*/
  public void polynomialEquation(String equation,PowerSeries s,String additional) {
	  String alteredEquation=("0+"+equation)
			  .replaceAll(" ", "")
			  .replaceAll("\\+\\+", "+")
			  .replaceAll("\\-\\+", "-")
			  .replaceAll("\\+\\-", "-")
			  .replaceAll("\\-\\-", "+")
			  .replaceAll("\\-", "+-")
			  .replaceAll(" ", "");
	  String[] sumParts=alteredEquation.split("\\+");
	  String[] coefficients=findStr("(?<!\\+\\-\\*)[^\\+\\-\\*]+?(?=\\^)",alteredEquation);
	  if(coefficients.length==0) throw new EquationFormatException("No valid coefficients have been found in the equation "+equation);
	  String var=coefficients[0];
	  for(String vars:coefficients) if(!vars.matches(var)) throw new EquationFormatException("There has been an invalid variablename fist variable: "+var+" invalid variable: "+vars);
	  String lineq="0";
	  for(String part:sumParts) {
		  if(part.contains(var)) {
			int exp=1;
		  	String sugestions[]=findStr("\\^\\d+",part);
		  	if(sugestions.length>=1) exp=Integer.parseInt(sugestions[0].substring(1));
		  	s.lowerPower(exp, "poly<"+var+"><"+exp+">");
		  	lineq+="+"+part.replace(var+"^"+exp, "poly<"+var+"><"+exp+">");
		  }else lineq+="+"+part;
	  }
	  linearEquation(lineq+additional);
	  
  }
  /**Defines multiplication. There are three lists of qubits and weights. Each pair build a number like a list of bits can resemble a number.
   * This command will add a condition that the number represented by targ should be the product of the numbers represented by n1 and n2.
   * @param targ qubits of the target
   * @param targw weights of the target
   * @param n1 qubits of the first factor
   * @param weights1 weights of the first factor
   * @param n2 qubits of the second factor
   * @param weights2 weights of the second factor
   * @param carriesL carries wich will be used to perform this multiplikation
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,int[],float[],int[], float[], int[][])*/
  public void multiply(int[] targ, float[] targw, int[] n1, float[] weights1, int[] n2, float[] weights2, int[]... carriesL) {
    //if(targ.length<n1.length+n2.length-1) throw new NotEnouthQubitsException("Not enouth qubits to save the result.\n Qubits needed:"+(n1.length+n2.length-1)+"\n Qubits present:"+(targ.length));
    int[] carries=toSingleArray(carriesL);
    if (carries.length<n1.length*n2.length) throw new NotEnoughQubitsException("Not enouth carries present! Carries needed: "+(n1.length*n2.length)+"Carries present: "+carries.length);
    float carriesCorrosp[]=new float[n1.length*n2.length];
    int[] cutCarries=new int[n1.length*n2.length];
    for (int i=0; i<cutCarries.length; i++) cutCarries[i]=carries[i];
    int runningIndex=0;
    for (int i=0; i<n1.length; i++) for (int j=0; j<n2.length; j++) {
      carriesCorrosp[runningIndex]=-weights1[i]*weights2[j];
      linkUnregistered(carries[runningIndex], n1[i], n2[j]);
      runningIndex++;
    }
    addQuadratic(0f, wrapArr(targw, carriesCorrosp), wrapArr(targ, carries));
  }
  /**Squares a variable. and stores the result in a new variable<br>
   * <b>Note: </b> This is <b>not</b> the same as {@code registerMultiplyCarries(target,var,var)} <br>
   * This method works with only half the carries.
   * @param target variable where the results are saved
   * @param var variable to be squared
   * @throws VarNotRegisteredException when trying to use a non-existent variable
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,String,String)
   * @see quva.core.QUBOMatrix#registerMultiply(String,String,String)
   * @see quva.core.QUBOMatrix#multiply(String,String,String)*/
  @Registers
  public void registerSquareCarries(String target,String var) {
	  int[] qubits=find(var);
	  float[] weights=findWeight(var);
	  LinkedList<Integer> qubitsReg=new LinkedList<Integer>();
	  LinkedList<Float> weightReg=new LinkedList<Float>();
	  for(int qubit:qubits) qubitsReg.add(qubit);
	  for(float weight:weights) weightReg.add(weight*weight);
	  for(int i=0;i<qubits.length-1;i++)
		  for(int j=i+1;j<qubits.length;j++) {
			  int newQubit=Qubits.next();
			  link(newQubit,qubits[i],qubits[j]);
			  float we=2*weights[i]*weights[j];
			  qubitsReg.add(newQubit);
			  weightReg.add(we);
		  }
	  int retInt[]=new int[qubitsReg.size()];
	  float retFloat[]=new float[qubitsReg.size()];
	  Object objInt[]=qubitsReg.toArray();
	  Object objFloat[]=weightReg.toArray();
	  for(int i=0;i<retInt.length;i++) retInt[i]=(int) objInt[i];
	  for(int i=0;i<objFloat.length;i++) retFloat[i]=(float) objFloat[i];
	  register(target,retInt,retFloat);
  }
  /**Registered the carries used in multiplication as own variable. The qubits will be chosen by {@code Qubits.next}
   * @param target name of the variable
   * @param n1 name of the first factor
   * @param n2 name of the second factor
   * @throws VarNotRegisteredException when trying to use a non-existent variable
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#registerMultiply(String,String,String)
   * @see quva.core.QUBOMatrix#multiply(String,String,String)*/
	@quva.core.Registers
  public void registerMultiplyCarries(String target, String n1, String n2) {
    int qubitsNumb=find(n1).length*find(n2).length;
    registerMultiplyCarries(target, find(n1), find(n2), Qubits.next(qubitsNumb));
  }
  /**Registered the carries used in multiplication as own variable.
   * @param target name of the variable
   * @param n1 name of the first factor
   * @param n2 name of the second factor
   * @param carriesL the carries that form the multiplication
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @throws VarNotRegisteredException when trying to use a non-existent variable
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#multiply(String,String,String)*/
	@quva.core.Registers
  public void registerMultiplyCarries(String target, String n1, String n2, int[] carriesL) {
    registerMultiplyCarries(target, find(n1), find(n2), carriesL);
  }
  /**Registered the carries used in multiplication as own variable.
   * @param target name of the variable
   * @param n1 qubits of the first factor
   * @param n2 qubits of the second factor
   * @param carriesL the carries that form the multiplication
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.VarNotRegisteredException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,String,String)
   * @see quva.core.QUBOMatrix#multiply(int[], int[], int[], int[][])*/
	@quva.core.Registers
  public void registerMultiplyCarries(String target, int[] n1, int[]n2, int[] carriesL) {
    registerMultiplyCarries(target, n1, findWeight(n1), n2, findWeight(n2), carriesL);
  }
  /**Registered the carries used in multiplication as own variable.
   * @param target name of the variable
   * @param n1 qubits of the first factor
   * @param weights1 qubits of the first factor
   * @param n2 qubits of the second factor
   * @param weights2 qubits of the second factor
   * @param carriesL the carries that form the multiplication
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,String,String)
   * @see quva.core.QUBOMatrix#multiply(int[],float[], int[],float[], int[],float[], int[][])*/
	@quva.core.Registers
  public void registerMultiplyCarries(String target, int[] n1, float[] weights1, int[] n2, float[] weights2, int[]... carriesL) {
    int[] carries=toSingleArray(carriesL);
    if (carries.length<n1.length*n2.length) throw new NotEnoughQubitsException("Not enouth carries present! Carries needed: "+(n1.length*n2.length)+"Carries present: "+carries.length);
    int[] cutCarries=new int[n1.length*n2.length];
    for (int i=0; i<cutCarries.length; i++) cutCarries[i]=carries[i];
    register(target, carries, multiplyCarries(n1, weights1, n2, weights2, carries));
  }
  /**Gives the weights of the carries used in a multiplication
   * @param n1 name of the first factor
   * @param n2 name of the second factor
   * @param carriesL carries used
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @throws VarNotRegisteredException when trying to use a non-existent variable
   * @return the weights involved in the multiplication
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,String,String)*/
  public float[] multiplyCarries(String n1, String n2, int[] carriesL) {
    return multiplyCarries( find(n1), find(n2), carriesL);
  }
  /**Gives the weights of the carries used in a multiplication
   * @param n1 qubits of the first factor
   * @param n2 qubits of the second factor
   * @param carriesL carries used
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @return the weights involved in the multiplication
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,int[],int[],int[])*/
  public float[] multiplyCarries(int[] n1, int[]n2, int[] carriesL) {
    return multiplyCarries( n1, findWeight(n1), n2, findWeight(n2), carriesL);
  }
  /**Gives the weights of the carries used in a multiplication
   * @param n1 qubits of the first factor
   * @param weights1 qubits of the first factor
   * @param n2 qubits of the second factor
   * @param weights2 qubits of the second factor
   * @param carriesL carries used
   * @throws NotEnoughQubitsException when there aren't enough carries
   * @return the weights involved in the multiplication
   * @see quva.core.NotEnoughQubitsException
   * @see quva.core.QUBOMatrix#registerMultiplyCarries(String,int[],float[],int[],float[],int[][])*/
  public float[] multiplyCarries(int[] n1, float[] weights1, int[] n2, float[] weights2, int[]... carriesL) {
    //if(targ.length<n1.length+n2.length-1) throw new NotEnouthQubitsException("Not enouth qubits to save the result.\n Qubits needed:"+(n1.length+n2.length-1)+"\n Qubits present:"+(targ.length));
    int[] carries=toSingleArray(carriesL);
    if (carries.length<n1.length*n2.length) throw new NotEnoughQubitsException("Not enouth carries present! Carries needed:"+(n1.length*n2.length)+"Carries present: "+carries.length);
    float carriesCorrosp[]=new float[n1.length*n2.length];
    int[] cutCarries=new int[n1.length*n2.length];
    for (int i=0; i<cutCarries.length; i++) cutCarries[i]=carries[i];
    int runningIndex=0;
    for (int i=0; i<n1.length; i++) for (int j=0; j<n2.length; j++) {
      carriesCorrosp[runningIndex]=weights1[i]*weights2[j];
      link(carries[runningIndex], n1[i], n2[j]);
      runningIndex++;
    }
    return carriesCorrosp;
  }
  /**Applies a matrix to a list of qubits. The qubits given are automaticly distributed in rows and cols. So giving a 10 numbers with a 5x5 matrix will cause it to distribute it in two 5-element array and apply put it in the rows described in the first group and in the columns described by the second group.
   * @param matrix matrix to be applied
   * @param vars qubtis given
   * @throws NotEnoughQubitsException when there aren't enough carries*/
  public void pattern(float[][] matrix, int[]... vars) {
    ArrayList<Integer> allVars=new ArrayList<Integer>();
    for (int[] varl : vars)for (int var : varl) allVars.add(var);
    if(allVars.size()<matrix.length+matrix[0].length) throw new NotEnoughQubitsException("Too few qubits are given. Given Qubits: "+allVars.size()+"\nNeeded Qubits: "+(matrix.length+matrix[0].length));
    int[] rowvar=new int[matrix.length];
    int[] colvar=new int[matrix[0].length];
    for (int i=0; i<rowvar.length; i++) rowvar[i]=allVars.get(i);
    for (int i=0; i<colvar.length; i++) colvar[i]=allVars.get(rowvar.length+i);
    for (int i=0; i<matrix.length; i++) for (int j=0; j<matrix[0].length; j++) add(matrix[i][j], rowvar[i], colvar[j]);
  }
  /**Adds the following condition: Exactly count of the given qubits should be one. If you want to allow n or n+1 qubits you can set count=n+0.5
   * @param count number of qubits allowed to be one
   * @param vars qubits involved
   * @see quva.core.QUBOMatrix#addQuadratic(float, Number...)*/
  public void limit(float count, int[]... vars) {
    int[] donearray=toSingleArray(vars);
    float ar[]=new float[donearray.length];
    for (int i=0; i<ar.length; i++) ar[i]=1;
    addQuadratic(-count, ar, donearray);
  }
  /**Generates a matrix that can be used as weights with a function and a set of weights. (This method should not be used unless you know what you are doing). The number of weights needs to match the number of inputs in the function.
   * @param p object that holds the function
   * @param inp involved weights
   * @return a matrix generated by the function*/
  public float[][] toPattern(CustomPattern p, float[]... inp) {
    float[] vals=toSingleArray(inp);
    float offset=p.f(zeroes(vals.length));
    float[][] ret=new float[vals.length][vals.length];
    for (int i=0; i<vals.length; i++) {
      float inputs[]=zeroes(vals.length);
      inputs[i]=vals[i];
      ret[i][i]=p.f(inputs)-offset;
    }
    for (int i=0; i<vals.length-1; i++) {
      for (int j=i+1; j<vals.length; j++) {
        float inputs[]=zeroes(vals.length);
        inputs[i]=vals[i];
        inputs[j]=vals[j];
        ret[i][j]=p.f(inputs)-ret[i][i]-ret[j][j]-offset;
      }
    }
    return ret;
  }
  /**Adds the condition that {@code q_target=q_v1(1-q_v2)}
   * @param target number of the target
   * @param v1 number of the first qubit
   * @param v2 number of the second qubit
   * @see quva.core.QUBOMatrix#iLink(int,int,int)
   * @see quva.core.QUBOMatrix#link(int,int,int)
   * @see quva.core.QUBOMatrix#carry(int,int,int)*/
  public void ilink(int target, int v1, int v2) {
    add(1, target);
    add(1, v1);
    add(-1, v1, v2);
    add(-2, v1, target);
    add(2, v2, target);
    assignedCarries.add(-target);
    assignedCarriesN1.add(v1);
    assignedCarriesN2.add(v2);
    assignedCarriesMode.add(0);
  }    
  /**Gives the matrix as a {@code String}
   * @return the matrix as {@code String}*/
  @Override
  public String toString() {
    QUBOMatrix q=new QUBOMatrix(matrix.clone());
    q.truncate();
    return q.convert_to_String();
  }
  /**Here the matrix is stored*/
  public float matrix[][];
  /**Here the matrix size is stored*/
  public int size;
  /**Here the m (weight for priorisation of conditions) is stored*/
  public float m=1000;
  /**Older version used this field to determine whether a matrix has been set up correctly*/
  public boolean initc=false;
  /**Older version used this field to store a message if the matrix wasn't set up correctly*/
  public String initm="";    
  /**Used to determine whether the layer one or layer 0 doesn't add weights to an expression*/
  public boolean begin_layering_with_one;
  /**Current layer. Conditions added in a higher layer have a higher priority that ones added in a lower one.*/
  public int layer=0;
  /**This method does the preprocessing*/
	public void procBegin(){
	    this.size=matrix.length;
		lastMatrix=this;
		quva.util.QUBOMatrixProcessor.process(this);
	}
  /**Default constructor used. This will create an empty new {@code QUBOMatrix} with the size 100*/
  public QUBOMatrix() {
	  this(100);
  }
  /**This constructor takes the size of the {@code QUBOMatrix} and creates an empty new matrix with that size
   * @param size size of the matrix*/
  public QUBOMatrix(int size) {
    Qubits.unassign(range(0,size-1));
    matrix=new float[size][size];
    for (int i=0; i<matrix.length; i++) 
      for (int j=0; j<matrix.length; j++) matrix[i][j]=0;
    this.size=size;
	  procBegin();
  }
  /**Creates a {@code QUBOMatrix} with a already given matrix
   * @param matrix given matrix*/
  public QUBOMatrix(float matrix[][]) {
    load(matrix);
	  procBegin();
  }
  /**Clones a QUBOMatrix into the current matrix
   * @param matrix the matrix to be loaded*/
  public void load(QUBOMatrix matrix) {
	  Field[] fields=QUBOMatrix.class.getDeclaredFields();
	  for(Field f:fields) {
		  if(Modifier.isTransient(f.getModifiers()))
			try {
				f.set(this, f.get(matrix));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
	  }
	}
  /**Runs a matrix without directly creating a {@code QUBOMatrix}. You can also attach {@code QuvaModifiers} and a {@code postProcessingHandler}.
   * @param matrix matrix to be run
   * @param modifiers modifiers to be applied
   * @param mode mode used for execution as used in {@link  quva.core.QUBOMatrix#execute(int[])}
   * @see quva.util.QuvaModifiers
   * @see quva.core.QUBOMatrix#execute(int[])
   * @return the values of the qubits*/
  public static int[] sample(float[][] matrix,QuvaModifiers modifiers,int... mode) {
	  QUBOMatrix mat=new QUBOMatrix(matrix);
	  modifiers.apply(mat);
	  int arr[]=mat.execute(mode);
	  mat.lastRes=arr;
	  mat.applyPostProcessing(modifiers.handler);
	  return arr;
  }
  /**Runs a matrix without directly creating a {@code QUBOMatrix}.
   * @param matrix matrix to be run
   * @param mode mode used for execution as used in {@link  quva.core.QUBOMatrix#execute(int[])}
   * @see quva.util.QuvaModifiers
   * @see quva.core.QUBOMatrix#execute(int[])
   * @return the values of the qubits*/
  public static int[] sample(float[][] matrix,int... mode) {
	  QUBOMatrix mat=new QUBOMatrix(matrix);
	  return mat.execute(mode);
  }
  /**Removes the qubit with the number a. This deletes the column and row with the number a.
   * @param a number of the qubit
   * @see quva.core.QUBOMatrix#remove(int,boolean)
   * @see quva.core.QUBOMatrix#truncate()*/
  public void remove(int a) {
		Map<int[],float[]> modified=new HashMap<int[],float[]>();
		
		for(String str: registeredVars.keySet()) {
			int[] qubits=registeredVars.get(str);
			float[] oldW=findWeight(str);
			for(int i=0;i<qubits.length;i++) if(qubits[i]==a) oldW[i]=0;
			for(int i=0;i<qubits.length;i++) if(qubits[i]>a) qubits[i]--;
			registeredVars.replace(str, qubits);
			modified.put(qubits, oldW);
		}/*
		for(int[] s: registeredWeights.keySet()) {
			float[] we=registeredWeights.get(s);
			for(int i=0;i<s.length;i++) {
				if(s[i]==a) we[i]=0;
				if(s[i]>=a) s[i]--;
				}
			modified.put(s, we);
		}*/
		registeredWeights=modified;
    QUBOMatrix ret=new QUBOMatrix(size-1);
    if (a==size) for (int i=0; i<size-1; i++)for (int j=0; j<size-1; j++) ret.matrix[i][j]=matrix[i][j]; 
    else
      if (a==0) for (int i=0; i<size-1; i++)for (int j=0; j<size-1; j++) ret.matrix[i][j]=matrix[i+1][j+1]; 
      else {
        for (int i=0; i<a; i++)for (int j=0; j<a; j++) ret.matrix[i][j]=matrix[i][j];
        for (int i=a+1; i<size; i++)for (int j=a+1; j<size; j++) ret.matrix[i-1][j-1]=matrix[i][j];
        for (int i=0; i<a; i++)for (int j=a+1; j<size; j++) ret.matrix[i][j-1]=matrix[i][j];
        for (int i=a+1; i<size; i++)for (int j=0; j<a; j++) ret.matrix[i-1][j]=matrix[i][j];
      }
    size--;
    matrix=ret.matrix;
  }
  /**Writes the current state of the matrix in a file, so it can be executed later
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#runDWave()
   * @see quva.core.QUBOMatrix#simulate()
   * @see quva.core.QUBOMatrix#process(String,int)*/
  public void push() {
	  File outputFilePython=new File(ExecuteProgram.execScriptsRoot,"ResultsFile.txt");
	  try {
		Files.write(outputFilePython.toPath(), toString().getBytes(StandardCharsets.UTF_8));
	} catch (IOException e) {
		e.printStackTrace();
	}
  }
  /**The last result processed is saved here*/
  public int[] lastRes;
  /**Reads the results returned by {@code simulate}/{@code runDWave} and returns the values of the qubits
   * @param result input to be read
   * @param mode info about the way the info was gathered either SIMULATE or ANNEAL
   * @return the solution of the {@code QUBOMatrix} as an integer array
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#runDWave()
   * @see quva.core.QUBOMatrix#simulate()
   * @see quva.core.QUBOMatrix#push()
   * @throws QuvaException when the result is a stacktrace of a pythonscript*/
  public int[] process(String result,int mode) {
	  if(result.contains("raise SolverAuthenticationError")) {
		  System.err.println("An error occured in the Python script (Propable cause: Invalid token)\n");
		  System.err.println(result);
		  throw new QuvaException("The python script raised an exception (Propable cause: Invalid token)");
	  }else
	  if(result.contains("ModuleNotFoundError")||result.contains("ImportError")) {
		  System.err.println("An error occured in the Python script (Propable cause: Lacking libraries)\n");
		  System.err.println("Libraries checklist:\ndwave-ocean-sdk (requires 64-bit version of python 3)\ndwave_qbsolv");
		  System.err.println(result);
		  throw new QuvaException("The python script raised an exception (Propable cause: Lacking libraries)");
	  }else
	  if(result.contains("RequestTimeout")) {
		  System.err.println("An error occured in the Python script (Propable cause: Timout because of lacking internet connection)\n");
		  System.err.println(result);
		  throw new QuvaException("The python script raised an exception (Propable cause: Timout because of lacking internet connection)");
	  }else
	  if(result.contains("Traceback (most recent call last):")) {
		  System.err.println("An error occured in the Python script (Cause: Unknown)\n");
		  System.err.println(result);
		  throw new QuvaException("The python script raised an exception (Cause: Unknown)");
	  }
	  if(mode==SIMULATE) {
		  String process=result.replaceAll("(?s)(.*?SampleSet\\(rec\\.array\\(\\[\\(\\[)", "");
		  process=process.replaceAll("(?s)(\\].*)", "").replaceAll(" ","");
		  String ret1[]=process.split(",");
		  int[] ret=new int[ret1.length];
		  for(int i=0;i<ret.length;i++) ret[i]=Integer.parseInt(ret1[i]);
		  lastRes=ret;
		  return ret;
	  }
	  if(mode==ANNEAL) {
		  String process=result.replaceAll("(?s)(.*?Sample\\(sample\\=\\{)", "");
		  process=process.replaceAll("(?s)(\\}, .*)", "");
		  process=process.replaceAll("\\d+?:", "").replaceAll(" ","");
		  String ret1[]=process.split(",");
		  int[] ret=new int[ret1.length];
		  for(int i=0;i<ret.length;i++) ret[i]=Integer.parseInt(ret1[i]);
		  lastRes=ret;
		  //System.out.println(ret[0]+" "+ret[1]+" "+ret[2]);
		  return ret;
	  }
	  return null;
  }
  /**Calculates the value of a variable based on a list of arbitrary qubits
   * @param values values of the qubits
   * @param qubits qubits involved
   * @return the value of the variable
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)
   * @see quva.core.QUBOMatrix#readVar(int[],String)*/
  public float inset(int[] values,int[] qubits) {
	  float val[]=registeredWeights.get(qubits);
	  float ret=0;
	  for(int i=0;i<val.length;i++) ret+=values[i]*val[i];
	  return ret;
  }
  /**Calculates the value of a variable based on a list of arbitrary qubits
   * @param values values of the qubits
   * @param var variables involved involved
   * @return the value of the variable
   * @see quva.core.QUBOMatrix#readVar(String)
   * @see quva.core.QUBOMatrix#readVar(int[],String)*/
  public float inset(int[] values,String var) {
	  float val[]=findWeight(var);
	  float ret=0;
	  for(int i=0;i<val.length;i++) ret+=values[i]*val[i];
	  return ret;
  }
  /**Reads the value of a variable from a list of qubits values
   * @param values values of the qubits
   * @param qubits qubits involved
   * @return the value of the variable
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)
   * @see quva.core.QUBOMatrix#readVar(int[],String)*/
  public float readVar(int[] values,int[] qubits) {
	  float val[]=registeredWeights.get(qubits);
	  float ret=0;
	  for(int i=0;i<val.length;i++) ret+=values[qubits[i]]*val[i];
	  return ret;
  }
  /**Reads the value of a variable from a list of qubits
   * @param values values of the qubits
   * @param var name of the variable
   * @return the value of the variable
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)*/
  public float readVar(int[] values,String var) {
	  return readVar(values,find(var));
  }
  /**Reads the value of a variable from the last result processed by {@code process}/{@code execute}
   * @param var name of the variable
   * @return the value of the variable based on the last result given
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(int[],String)*/
  public float readVar(String var) {
	  return readVar(lastRes,find(var));
  }
  /**Reads the values of all variables and puts them in a {@code HashMap}
   * @return HashMap of the variable names together with the values based on the last results given
   * @see quva.core.QUBOMatrix#readVar(String)
   * @see quva.core.QUBOMatrix#readVar(int[],String)*/
  public HashMap<String,Float> readAllVars(){
	  HashMap<String,Float> ret=new HashMap<String,Float>();
	  for(String var:registeredVars.keySet()) ret.put(var,readVar(var));
	  return ret;
  }
  /**Applies a array of {@link PostProcessingHandler} to be applied
   * @param h handlers to be applied*/
  public void applyPostProcessing(PostProcessingHandler... h) {
	  BundledHandler handler=new BundledHandler(h);
	  handler.postprocessing(this);
  }
  /**Reads the values of all variables and puts them in a {@code HashMap}
   * @param results read qubits
   * @return HashMap of the variable names together with the values
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)*/
  public HashMap<String,Float> readAllVars(int[] results){
	  HashMap<String,Float> ret=new HashMap<String,Float>();
	  for(String var:registeredVars.keySet()) ret.put(var,readVar(results,var));
	  return ret;
  }
  /**Executes the written matrix and returns the values of the qubits. The results can be processed using {@code readVar}/{@code readAllVars}
   * @param mode way the hamilton matrix is processed<br>SIMULATE uses qubosolv to simulate the quantumannealer<br>ANNEAL sends the matrix to a real quantumannealer<br>You can add AUTOTRUNCATE to adjust the qubits of the variables
   * @param push determines whether to refresh the saved matrix
   * @return the solution of the {@code QUBOMatrix} as an {@code int[]}
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)*/
  public int[] execute(int mode,boolean push) {
	  return super.execute(mode, push, this);
  }
  /**Executes the written matrix and returns the values of the qubits wich are then used by {@code readVar}/{@code readAllVars}. Automatically pushes.
   * @param mode way the hamilton matrix is processed<br>SIMULATE uses qubosolv to simulate the quantumannealer<br>ANNEAL sends the matrix to a real quantumannealer.<br>
   * See the documentation of {@link quva.core.ExecuteProgram#execute(int, boolean, QUBOMatrix)} for more details.
   * @return the solution of the {@code QUBOMatrix} as an {@code int[]}
   * @see quva.core.ExecuteProgram#execute(int, boolean, QUBOMatrix)
   * @see quva.core.QUBOMatrix#execute(int,boolean)
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)*/
  public int[] execute(int mode) {
	  return execute(mode,true);
  }
  /**Executes the written matrix and returns the values of the qubits wich are then used by {@code readVar}/{@code readAllVars}. Automatically pushes.
   * @param mode way the hamilton matrix is processed<br>SIMULATE uses qubosolv to simulate the quantumannealer<br>ANNEAL sends the matrix to a real quantumannealer.<br>
   * See the documentation of {@link quva.core.ExecuteProgram#execute(int, boolean, QUBOMatrix)} for more details. The difference to {@link quva.core.QUBOMatrix#execute(int)} is that you can seperate different modifierts like SIMULATE and AUTOTRUNCATE with a ",".
   * @return the solution of the {@code QUBOMatrix} as an {@code int[]}
   * @see quva.core.ExecuteProgram#execute(int, boolean, QUBOMatrix)
   * @see quva.core.QUBOMatrix#execute(int,boolean)
   * @see quva.core.QUBOMatrix#readAllVars()
   * @see quva.core.QUBOMatrix#readVar(String)*/
  public int[] execute(int... mode) {
	  return execute(sumUp(mode),true);
  }
  /**Simulates the QUBO problem
   * @return the results of the simulation as given the python script (process can convert these into the values of the qubits)
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#push()
   * @see quva.core.QUBOMatrix#runDWave()
   * @see quva.core.ExecuteProgram#simulateStatic()*/
  public String simulate() {
	  return super.simulateStatic();
  }
  /**Runs your {@code QUBOMatrix} on a DWave Quantumannealer returns the best sample
   * @return the results of the quantum-annealer as given the python script (process can convert these into the values of the qubits)
   * @see quva.core.QUBOMatrix#execute(int[])
   * @see quva.core.QUBOMatrix#push()
   * @see quva.core.QUBOMatrix#simulate()
   * @see quva.core.ExecuteProgram#runDWaveStatic()*/
  public String runDWave() {
	  return super.runDWaveStatic();
  }
  /**Removes the qubit with the number a. This deletes the column and row with the number a.
   * One can choose if the program should assume the qubit should have been one or 0.
   * @param a number of the qubit
   * @param as_one determines if it should be assumed that the qubit was one.
   * @see quva.core.QUBOMatrix#remove(int)*/
  public void remove(int a, boolean as_one) {
    if (as_one) for (int i=0; i<matrix.length; i++) matrix[i][i]+=matrix[Math.min(i, a)][Math.max(i, a)];
    remove(a);
  }
  /**Calls ilink
   * @param target number of the target qubit
   * @param v1 number of the first qubit
   * @param v2 number of the second qubit
   * @see quva.core.QUBOMatrix#ilink(int,int,int)
   * @see quva.core.QUBOMatrix#carry(int,int,int)
   * @see quva.core.QUBOMatrix#link(int,int,int)*/
  public void iLink(int target, int v1, int v2) {
    ilink(target, v1, v2);
  }    
  /**Removes all qubits that don't have a bias or interation with other qubits.
   * @see quva.core.QUBOMatrix#remove(int)*/
  public void truncate() {
    for (int i=size-1; i>=0; i--) {
      boolean re=true;
      for (int j=0; j<size; j++) { 
        re=re?(matrix[i][j]==0&&matrix[j][i]==0):false;
      }
      if (re) remove(i);
    }
  }
  /**Sets m and begin_layering_with_one (calls init)
   * @param m value for m
   * @param begin_layering_with_one value for begin_layering_with_one
   * @see quva.core.QUBOMatrix#init(float,boolean)*/
  public void setup(float m, boolean begin_layering_with_one) {
    init(m, begin_layering_with_one);
  }
  /**Sets m and begin_layering_with_one
   * @param m value for m
   * @param begin_layering_with_one value for begin_layering_with_one
   * @see quva.core.QUBOMatrix#setup(float,boolean)*/
  public void init(float m, boolean begin_layering_with_one) {
    this.m=m;
    this.begin_layering_with_one=begin_layering_with_one;
    initc=true;
  }
  /**Sets m 
   * @param m value for m
   * @see quva.core.QUBOMatrix#init(float,boolean)*/
  public void init(float m) {
    init(m,false);
  }
  /**Selects the current layer in wich conditions are added
   * @param layer new layer
   * @see quva.core.QUBOMatrix#la(int)
   * @see quva.core.QUBOMatrix#init(float,boolean)
   * @see quva.core.QUBOMatrix#init(float)*/
  public void layer(int layer) {
    this.layer=layer-(begin_layering_with_one?1:0);
  }
  /**Selects the current layer in wich conditions are added (calls layer)
   * @param layer new layer
   * @see quva.core.QUBOMatrix#layer(int)
   * @see quva.core.QUBOMatrix#init(float,boolean)
   * @see quva.core.QUBOMatrix#init(float)*/
  public void la(int layer) {
    layer(layer);
  }
  /**Factor set by {@link #amplify(float)}*/
  public float amp=1;
  /**Amplifies all weights added. This can help the quantumannealer find a correct sollution.
   * @param amp factor to be set.*/
  public void amplify(float amp) {
	  this.amp=amp;
  }
  /**Adds a value to the entry the weight between qubit a and qubit b. This will be scaled by the selected layer and amplification.
   * @param v value to be added
   * @param a number of the first qubit
   * @param b number of the second qubit
   * @see quva.core.QUBOMatrix#layer(int)
   * @see quva.core.QUBOMatrix#add(double,int)*/
  public void add(float v, int a, int b) {
    matrix[Math.min(a, b)][Math.max(a, b)]+=amp*Math.pow(m, layer)*v;
  }
  /**Adds a value to the entry the weight between qubit a and qubit b. This will <b>not</b> be scaled by the selected layer and amplification.
   * @param v value to be added
   * @param a number of the first qubit
   * @param b number of the second qubit
   * @see quva.core.QUBOMatrix#layer(int)
   * @see quva.core.QUBOMatrix#add(double,int)*/
  public void addBasic(float v, int a, int b) {
    matrix[Math.min(a, b)][Math.max(a, b)]+=v;
  }
  /**Sets a value to the entry the weight between qubit a and qubit b.
   * @param v value to be inserted
   * @param a number of the first qubit
   * @param b number of the second qubit
   * @see quva.core.QUBOMatrix#layer(int)
   * @see quva.core.QUBOMatrix#add(double,int)*/
  public void set(float v, int a, int b) {
    matrix[Math.min(a, b)][Math.max(a, b)]=v;
  }
  /**Adds a value to the bias of a qubit scaled b the current layer.
   * @param v value to be added
   * @param a number of the qubit
   * @see quva.core.QUBOMatrix#layer(int)
   * @see quva.core.QUBOMatrix#add(float,int,int)*/
  public void add(double v, int a) {
    matrix[a][a]+=Math.pow(m, layer)*v*amp;
  }
  /**Replaces the current matrix with a new one
   * @param matrix matrix to replace the old one*/
  public void load(float matrix[][]) {
    this.matrix=matrix;
    size=matrix.length;
  }
  /**Adds a carry that makes {@code q_target=q_v1*q_v2}
   * @param target number of the target qubit
   * @param v1 number of the first qubit
   * @param v2 number of the second qubit
   * @see quva.core.QUBOMatrix#ilink(int,int,int)
   * @see quva.core.QUBOMatrix#carry(int,int,int)
   * @see quva.core.QUBOMatrix#iLink(int,int,int)*/
  public void link(int target, int v1, int v2) {
    add(3, target);
    add(1, v1, v2);
    add(-2, v1, target);
    add(-2, v2, target);
    assignedCarries.add(target);
    assignedCarriesN1.add(v1);
    assignedCarriesN2.add(v2);
    assignedCarriesMode.add(1);
  }
  /**Adds a carry that makes {@code q_target=q_v1*q_v2} it will however no be listed as a carry
   * @param target number of the target qubit
   * @param v1 number of the first qubit
   * @param v2 number of the second qubit
   * @see quva.core.QUBOMatrix#ilink(int,int,int)
   * @see quva.core.QUBOMatrix#carry(int,int,int)
   * @see quva.core.QUBOMatrix#iLink(int,int,int)*/
  public void linkUnregistered(int target, int v1, int v2) {
    add(3, target);
    add(1, v1, v2);
    add(-2, v1, target);
    add(-2, v2, target);
    assignedCarries.add(target);
    assignedCarriesN1.add(-v1);
    assignedCarriesN2.add(-v2);
    assignedCarriesMode.add(2);
  }
  /**Adds a quadratic expression of the following form: {@code factor*(number+q_variable[0]*factors2[0]+...+q_variable[n]*factors2[n])^2} this will be weighted with the current layer.
   * @param factor factor to be multiplied
   * @param number constant part
   * @param factors2 weights of the qubits
   * @param variables qubits given
   * @see quva.core.QUBOMatrix#addQuadratic(float,Number[])
   * @see quva.core.QUBOMatrix#addQuadratic(float,float[],int[])*/
  public void addQuadratic(float factor, float number, float[] factors2, int[] variables) {
    if (!initc) System.out.println(initm);
    for (int i=0; i<factors2.length; i++) for (int j=0; j<factors2.length; j++) add(factor*(i==j?factors2[i]*factors2[j]:factors2[i]*factors2[j]), variables[i], variables[j]);
    for (int i=0; i<factors2.length; i++) matrix[variables[i]][variables[i]]+=Math.pow(m, layer)*factor*2*number*factors2[i];
  }
  /**Adds a quadratic expression of the following form: {@code (number+q_variable[0]*factors2[0]+...+q_variable[n]*factors2[n])^2} this will be weighted with the current layer.
   * @param number constant part
   * @param factors2 weights of the qubits
   * @param variables qubits given
   * @see quva.core.QUBOMatrix#addQuadratic(float,Number[])
   * @see quva.core.QUBOMatrix#addQuadratic(float,float[],int[])*/
  public void addQuadratic(float number, float[] factors2, int[] variables) {
    if (!initc) System.out.println(initm);
    for (int i=0; i<factors2.length; i++) for (int j=0; j<factors2.length; j++) add((i==j?factors2[i]*factors2[j]:factors2[i]*factors2[j]), variables[i], variables[j]);
    for (int i=0; i<factors2.length; i++) matrix[variables[i]][variables[i]]+=Math.pow(m, layer)*2*number*factors2[i];
  }
  /**This will distribute the given numbers across two arrays the first half are the factors and the second the qubits
   * @param number constant part
   * @param vars given numbers
   * @see quva.core.QUBOMatrix#addQuadratic(float,float[],int[])*/
  public void addQuadratic(float number, Number... vars) {
    int len=vars.length>>1;
    float[] factors2=new float[len];
    int[] variables=new int[len];
    for (int i=0; i<len; i++) factors2[i]=Float.parseFloat(vars[i]+"");
    for (int i=0; i<len; i++) variables[i]=Integer.parseInt(vars[i+len]+"");
    if (!initc) System.out.println(initm);
    for (int i=0; i<factors2.length; i++) for (int j=0; j<factors2.length; j++) add((i==j?factors2[i]*factors2[j]:factors2[i]*factors2[j]), variables[i], variables[j]);
    for (int i=0; i<factors2.length; i++) matrix[variables[i]][variables[i]]+=Math.pow(m, layer)*2*number*factors2[i];
  }
  /**This will distribute the given numbers across two arrays the first half are the factors and the second the qubits
   * @param number constant part
   * @param vars given numbers
   * @see quva.core.QUBOMatrix#addQuadratic(float,float[],int[])*/
  public void addQuadratic(double number, Number... vars) {
    int len=vars.length>>1;
    float[] factors2=new float[len];
    int[] variables=new int[len];
    for (int i=0; i<len; i++) factors2[i]=Float.parseFloat(vars[i]+"");
    for (int i=0; i<len; i++) variables[i]=Integer.parseInt(vars[i+len]+"");
    if (!initc) System.out.println(initm);
    float nb=(float)number;      
    for (int i=0; i<factors2.length; i++) for (int j=0; j<factors2.length; j++) add((i==j?factors2[i]*factors2[j]:factors2[i]*factors2[j]), variables[i], variables[j]);
    for (int i=0; i<factors2.length; i++) matrix[variables[i]][variables[i]]+=Math.pow(m, layer)*2*nb*factors2[i];
  }
  /**Adds a quadratic expression with the all inputs not in arrays. calls {@code addQuadratic} with the numbers in vars distributed across two arrays.
   * @param number constant part of the linear expression inside
   * @param vars numbers given
   * @see quva.core.QUBOMatrix#addQuadratic(float,Number[])*/
  public void addq(double number, Number... vars) {
    addQuadratic(number, vars);
  }
  public QUBOMatrix objectClone() {
	  try {
		return (QUBOMatrix) clone();
	} catch (CloneNotSupportedException e) {
		e.printStackTrace();
		return null;
	}
  }
  /**Converts the matrix into a {@code String}. It won't be automatically truncated though.
   * @return the matrix as {@code String}
   * @see quva.core.QUBOMatrix#toString()*/
  public String convert_to_String() {
    String s="";
    for (int i=0; i<size-1; i++) {
      for (int j=0; j<size-1; j++) s+=matrix[i][j]+" ";
      s+=matrix[i][size-1]+"";
      s+="\n";
    }
    for (int j=0; j<size-1; j++) s+=matrix[size-1][j]+" ";
    s+=matrix[size-1][size-1]+"";
    return s;
  }
  /**Converts the matrix into a string that can be used as LaTeX code. It won't be automatically truncated though.
   * @return the matrix as a LaTeX command
   * @see quva.core.QUBOMatrix#toString()
   * @see quva.core.QUBOMatrix#convert_to_Latex(boolean)*/
  public String convert_to_Latex() {
    String s="\\begin{pmatrix}";
    for (int i=0; i<size-1; i++) {
      for (int j=0; j<size-1; j++) s+=matrix[i][j]+"&";
      s+=matrix[i][size-1]+"";
      s+="\\\\";
    }
    for (int j=0; j<size-1; j++) s+=matrix[size-1][j]+"&";
    s+=matrix[size-1][size-1]+"\\end{pmatrix}";
    return s;
  }
  /**Converts the matrix into a string that can be used as LaTeX code. You can select whether to put it into a pmatrix. It won't be automatically truncated though.
   * @param pmatrix determines whether to use a pmatrix
   * @return the matrix as a LaTeX command
   * @see quva.core.QUBOMatrix#toString()
   * @see quva.core.QUBOMatrix#convert_to_Latex()*/
  public String convert_to_Latex(boolean pmatrix) {
    String s="\\begin{"+(pmatrix?"p":"")+"matrix}";
    for (int i=0; i<size-1; i++) {
      for (int j=0; j<size-1; j++) s+=matrix[i][j]+"&";
      s+=matrix[i][size-1]+"";
      s+="\\\\";
    }
    for (int j=0; j<size-1; j++) s+=matrix[size-1][j]+"&";
    s+=matrix[size-1][size-1]+"\\end{"+(pmatrix?"p":"")+"matrix}";
    return s;
  }
}