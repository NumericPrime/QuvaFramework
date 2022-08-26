package quva.postprocessing;
/**Defines the way the values of the variables are output. Used by {@link OutputVarValues}*/
public interface OutputStyle{
	/**Generates a {@code String} for outputing the value of a variable
	 * @param var name of the variable
	 * @param value value of the variable
	 * @return String to be inserted*/
	public abstract String generateLine(String var,float value);
}
