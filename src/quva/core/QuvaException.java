package quva.core;
/**Superclass of all exception thrown by Quva*/
public class QuvaException extends RuntimeException {
	/**{@inheritDoc}*/
	public QuvaException() {
	}

	/**{@inheritDoc}*/
	public QuvaException(String message) {
		super(message);
	}

	/**{@inheritDoc}*/
	public QuvaException(Throwable cause) {
		super(cause);
	}

	/**{@inheritDoc}*/
	public QuvaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**{@inheritDoc}*/
	public QuvaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
