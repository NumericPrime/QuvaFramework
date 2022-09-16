package quva.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import quva.postprocessing.*;

/**Utility class extended by {@code ExecuteProgram}. Contains a variety of utility functions and constants.
 * @see quva.core.ExecuteProgram*/
public class QuvaUtilities{
	/**Used to signal to a QuvaApplication that it won't be executed
	 * @see quva.core.QuvaApplication#QuvaApplication(int)*/
	public final static int NONE=-1;
	/**Used as parameter to indicate a simulation
	 * @see quva.core.QUBOMatrix#execute(int)
	 * @see quva.core.QuvaApplication#QuvaApplication(int)*/
	public final static int SIMULATE=0;
	/**Used as parameter to indicate a anneal
	 * @see quva.core.QUBOMatrix#execute(int)
	 * @see quva.core.QuvaApplication#QuvaApplication(int)*/
	public final static int ANNEAL=1;
	/**Used as parameter to indicate that the results should be truncated
	 * @see quva.core.QUBOMatrix#execute(int)
	 * @see quva.core.QuvaApplication#QuvaApplication(int)*/
	public static final int AUTOTRUNCATE=2;
	/**Used as parameter to indicate delayed execution
	 * @see quva.core.QuvaApplication#QuvaApplication(int)
	 * @see quva.core.QuvaApplication#execute()*/
	public final static int DELAYED=4;
	/**Automatically sets the chain-strength to half the highest weight
	 * @see quva.core.QuvaApplication#QuvaApplication(int)
	 * @see quva.core.QUBOMatrix#execute(int)*/
	public final static int AUTOCHAINSTRENGTH=8;
	/**Makes execution ignore settings specified by {@link quva.core.QuvaExecutionSettings#executionSettings(int...)}
	 * @see quva.core.QuvaApplication#QuvaApplication(int)
	 * @see quva.core.QUBOMatrix#execute(int)
	 * @see quva.core.QuvaExecutionSettings#executionSettings(int[])*/
	public final static int CUSTOMSETTINGS=16;
	/**A {@link PostProcessingHandler} that prints the values of all vars.
	 * @see QUBOMatrix#applyPostProcessing(PostProcessingHandler[])*/
	public static final PostProcessingHandler PRINTALLVARS=new OutputVarValues();
	/**A {@link PostProcessingHandler} that arranges the qubits in a square grid.
	 * @see QUBOMatrix#applyPostProcessing(PostProcessingHandler[])*/
	public static final PostProcessingHandler OUTPUTSQUARE=new OutputGrid(0,(val,i,i2,i3)->val+" ");
	/**A {@link PostProcessingHandler} that arranges the qubits in a single line.
	 * @see QUBOMatrix#applyPostProcessing(PostProcessingHandler[])*/
	public static final PostProcessingHandler OUTPUTROW=new OutputGrid(-1,(val,i,i2,i3)->val+" ");
	/**A {@link PostProcessingHandler} that arranges the qubits in a single column.
	 * @see QUBOMatrix#applyPostProcessing(PostProcessingHandler[])*/
	public static final PostProcessingHandler OUTPUTCOL=new OutputGrid(1);
	/**Reads a file as String
	 * @param path file path
	 * @return contents of the file*/
	public static String readFile(String path) {
		  byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			  return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	  /**Puts given int inputs in an array
	   * @param inp inputs given
	 * @return array consisting of the input*/
	  public static int[] wrap(int... inp) {
	    return inp;
	  }
	  /**Puts given float iputs in an array
	   * @param inp inputs given
	 * @return array consisting of the input*/
	  public static float[] wrap(float... inp) {
	    return inp;
	  }
	  /**Does the same as wrapArr
	   * @param vars inputs given
	   * @return joined array*/
	  public static int[] toSingleArray(int[]... vars) {
	    ArrayList<Integer> allVars=new ArrayList<Integer>();
	    for (int[] varl : vars)for (int var : varl) allVars.add(var);
	    int[] donearray=new int[allVars.size()];
	    for (int i=0; i<donearray.length; i++) donearray[i]=allVars.get(i);
	    return donearray;
	  }
	  /**Does the same as wrapArr
	   * @param vars inputs given
	   * @return joined array*/
	  public static float[] toSingleArray(float[]... vars) {
	    ArrayList<Float> allVars=new ArrayList<Float>();
	    for (float[] varl : vars)for (float var : varl) allVars.add(var);
	    float[] donearray=new float[allVars.size()];
	    for (int i=0; i<donearray.length; i++) donearray[i]=allVars.get(i);
	    return donearray;
	  }
	  /**Generates an array with only 0's.
	   * @param l length of the array
	   * @return array consisting of 0's with given length*/
	  public static float[] zeroes(int l) {
	    float ret[]=new float[l];
	    for (int i=0; i<ret.length; i++) ret[i]=0;
	    return ret;
	  }
	  /**Gives an array of all {@code int} between begin and end
	   * @param begin begin of the range
	   * @param end of the range
	   * @return array consisting of the integers between begin and end*/
	  public static int[] range(int begin, int end) {
	    return range(begin, end, 1);
	  }
	  /**Gives an array of all {@code int} between begin and end while incrementing by step
	   * @param begin begin of the range
	   * @param end end of the range
	   * @param step increment value
	   * @return array consisting of the integers between begin and end with defined incre*/
	  public static int[] range(int begin, int end, int step) {
	    int ret[]=new int[((end-begin)/step)+1];
	    for (int i=0; i<ret.length; i++) ret[i]=begin+i*step;
	    return ret;
	  }
	  /**Puts a single {@code int} into an array
	   * @param a {@code int} given
	   * @return array consisting of one integer*/
	  public static int[] range(int a) {
	    return new int[]{a};
	  }
	  /**Joins the given {@code int[]} arrays into one
	   * @param inp inputs given
	   * @return joined array*/
	  public static int[] wrapArr(int[]... inp) {
	    return toSingleArray(inp);
	  }
	  /**Joins the given {@code float[]} arrays into one
	   * @param inp inputs given
	   * @return joined array*/
	  public static float[] wrapArr(float[]... inp) {
	    return toSingleArray(inp);
	  }
	/**Gets the size of the file in byte
	 * @param path file path
	 * @return size of the file*/
	public static int fileSize(String path) {
		try {
			return Files.readAllBytes(Paths.get(path)).length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	/**Prints a list of objects seperated by comma
	 * @param onj list of objects to be printed*/
	public static void println(Object... onj) {
		for(Object o:onj) System.out.println(o);
	}
	/**Returns the number of digits in the
	 * binary decomposition
	 * @param input number that is used for the binary decomposition
	 * @return number of binary digits*/
	public static int binaryDigits(int input) {
		return Integer.toBinaryString(input).length();
		}
	/**Logarithm with base 2
	 * @param input input of the logarithm
	 * @return logarithm with the base 2*/
	public static float log2(float input) {
		return (float)(Math.log(input)/Math.log(2d));
	}
	/**Powers of two
	 * @param power of two to be calculated
	 * @return power with the base 2*/
	public static int pow2(int power) {
		return 1<<power;
	}
	/**Gets a digit in the binary decomposition
	 * @param input int to be decomposed
	 * @param index number of the digit
	 * @return binary digit at the a certain place*/
	public static int binaryAt(int input,int index) {
		return (input>>index)&1;
	}
	/**Find all instances of a regex in a {@code String}
	 * @param regex {@code String} regex used
	 * @param input input to be searched
	 * @return all instances of a regex in a {@code String}*/
	public static String[] findStr(String regex,String input) {
		Pattern p=Pattern.compile(regex);
		Matcher m=p.matcher(input);
		ArrayList<String> outp=new ArrayList<String>();
		while(m.find()) {
			outp.add(m.group());
		}
		String[] ret = outp.toArray(new String[] {});
		return ret;
	}
	/**This method always returns true. You can use QuvaUtilities::retTrue instead of (i)-&gt;true wich makes the code more readable.
	 * @param i the number of the qubit
	 * @return always returns true*/
	public static boolean retTrue(int i) {
		return true;
		}
	/**This method always returns true. You can use QuvaUtilities::retTrue instead of (i,j)-&gt;true wich makes the code more readable.
	 * @param i the number of the first qubit
	 * @param j the number of the second qubit
	 * @return always returns true*/
	public static boolean retTrue(int i,int j) {
		return true;
		}
	/**Sums up the given {@code Integers}.
	 * @param ints {@code Integers} to be added
	 * @return the sum of the inputs*/
	public static int sumUp(int... ints) {
		int ret=0;
		for(int i:ints) ret+=i;
		return ret;
	}
	/**Converts an array of ints to an array of floats
	 * @param arr array to be convert
	 * @return array of floats with the same value*/
	public static float[] toFloatArray(int[] arr) {
		float ret[]=new float[arr.length];
		for(int i=0;i<arr.length;i++) ret[i]=(float) arr[i];
		return ret;
	}
	/**Converts an array of floats to an array of ints
	 * @param arr array to be convert
	 * @return array of floats with the same value*/
	public static int[] toIntArray(float[] arr) {
		int ret[]=new int[arr.length];
		for(int i=0;i<arr.length;i++) ret[i]=(int) arr[i];
		return ret;
	}
}