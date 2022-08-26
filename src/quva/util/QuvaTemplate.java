package quva.util;

import quva.core.QUBOMatrix;

/**This will allow setting Templates for certain problems*/
public interface QuvaTemplate {
	/**Creates a {@code QUBOMatrix}
	 * @return done {@code QUBOMatrix}*/
	public abstract QUBOMatrix run();
	/**Loads a Template
	 * @param tm template to be loaded
	 * @return {@code QUBOMatrix} extracted from the template*/
	public static QUBOMatrix load(QuvaTemplate tm) {
		QUBOMatrix done=tm.run();
		done.truncate();
		return done;
	}
}
