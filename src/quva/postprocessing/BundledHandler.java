package quva.postprocessing;

import java.util.*;

import quva.core.QUBOMatrix;

/**Bundles PostProcessingHandlers to be executed in order. This is used to add multiple PostProcessingHandlers.
 * @see quva.postprocessing.PostProcessingHandler*/
public class BundledHandler extends LinkedList<PostProcessingHandler> implements PostProcessingHandler {
	private static final long serialVersionUID = 7830634933264918165L;
	public BundledHandler(PostProcessingHandler... h) {
		for(PostProcessingHandler handler:h) add(handler);
	}
	/**{@inheritDoc}*/
	@Override
	public void postprocessing(QUBOMatrix m, Map<String, Float> mp, int[] res) {
		for(PostProcessingHandler h:this) h.postprocessing(m, mp, res);
	}

}
