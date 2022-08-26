package quva.postprocessing;

import java.util.Map;
import quva.core.QUBOMatrix;

/**PostProcessingHandlers help organize the post-processing of the results. It can be applied in the {@code QUBOMatrix} itself. But can also be applied to a QuvaApplication.
 * This API is designed to be very flexible and easy to use. 
 * @see quva.core.QUBOMatrix#applyPostProcessing(PostProcessingHandler[])
 * @see quva.core.QuvaApplication#setPostProcessingHandler(PostProcessingHandler[])
 * @see quva.util.QuvaModifiers#attach(PostProcessingHandler[])
 * @see OutputGrid
 * @see OutputVarValues
 * @see OutputForm*/
public interface PostProcessingHandler {
	/**This method does the main post-processing it reads the matrix,the values of the variables, and the values of the qubits.
	 * @param m the {@code QUBOMatrix} the results of wich will be read
	 * @param mp the name and values of the variables
	 * @param res the values of the qubits*/
	abstract void postprocessing(QUBOMatrix m,Map<String,Float> mp,int[] res);
	/**Reads the values of all variables from the QUBOMatrix and calls {@link #postprocessing(QUBOMatrix, Map, int[])} with it.
	 * @param m the {@code QUBOMatrix} the results of wich will be read.
	 * @param res the values of the qubits*/
	default void postprocessing(QUBOMatrix m,int[] res) {
		postprocessing(m,m.readAllVars(res),res);
	}
	/**Reads the last result from the QUBOMatrix and calls {@link #postprocessing(QUBOMatrix, int[])} with it.
	 * @param m the {@code QUBOMatrix} the results of wich will be read.*/
	default void postprocessing(QUBOMatrix m) {
		postprocessing(m,m.lastRes);
	}
}
