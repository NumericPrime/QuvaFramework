package quva.construct;

/**This interface is used by SafeCarries to determine where to apply the carries needed
 * @see quva.construct.SafeConstruct*/
public interface CarryRule {
	/**Assigns a array consisting of <b>two</b> boolean[] containing info on where to put the carries. 
	 * You should be good to go if the result doesn't contain the starting array and if for the result ret the following statement is true:<br>
	 * For all i a[i]==ret[0][i]||ret[1][i] is true.
	 * There is a standard one provided by SafeConstruct.
	 * @see quva.construct.SafeConstruct#standardRule
	 * @param a array to be processed
	 * @return a 2d array as explained above*/
	public boolean[][] assign(boolean[] a);
}
