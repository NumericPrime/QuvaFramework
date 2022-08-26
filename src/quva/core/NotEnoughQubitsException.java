package quva.core;

/**This exception gets thrown when trying to use multiplication with too few carries*/
public class NotEnoughQubitsException extends QuvaException {
	private static final long serialVersionUID = -9104967561559130372L;
	/**{@inheritDoc}*/
	public NotEnoughQubitsException() {
	}
	/**{@inheritDoc}*/
	public NotEnoughQubitsException(String message) {
		super(message);
	}
	/**{@inheritDoc}*/
	public NotEnoughQubitsException(Throwable cause) {
		super(cause);
	}
	/**{@inheritDoc}*/
	public NotEnoughQubitsException(String message, Throwable cause) {
		super(message, cause);
	}
	/**{@inheritDoc}*/
	public NotEnoughQubitsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
