package quva.construct;

/**The base of the Construct API<br>this can be used to allow the calculation of arbitrary function using the quantumannealer<br>
 * This interface manages the assignment of qubits for these purposes. The implementation by SafeCarries also handles the creation of carries.
 * @see quva.construct.CalculateFunction
 * @see quva.construct.CalculatePattern
 * @see quva.construct.SafeConstruct*/
public interface QuvaConstruct {
	/**This function is used to get the assignments of the products of the qubits of a variable to a certain qubit.<br>
	 * If you e.g. take x=8x_0+4x_1+2x_2+x_3 it saves the qubit where e.g x_0x_1 is saved.
	 * @return info about the qubits involved*/
	public abstract QuvaConstructRegister getRegistry();
	/**This function handles all functionality beyond providing the info about the qubtis used like creating carries. This function is <b>not</b> to be called befor getRegistry! In fact it is most likely be called after.*/
	public default void process() {}
	/**This provides info about whether a qubit is safe to be removed from the registry or if there are e.g. dependencies given through the carries.
	 * @param b combination to be checked
	 * @return true if the qubit may be removed, false if not*/
	public default boolean authorizeRemoval(boolean[] b) {
		return false;
	}
}
