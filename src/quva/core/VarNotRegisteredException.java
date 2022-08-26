package quva.core;

/**This Exception is thrown when trying to use a unregistered variable*/
public class VarNotRegisteredException extends QuvaException {
	private static final long serialVersionUID = 7523564056610100936L;
	/**{@inheritDoc}*/
	public VarNotRegisteredException() {
	}
	/**{@inheritDoc}*/
	public VarNotRegisteredException(String message) {
		super(message);
	}
	/**{@inheritDoc}*/
	public VarNotRegisteredException(Throwable cause) {
		super(cause);
	}
	/**{@inheritDoc}*/
	public VarNotRegisteredException(String message, Throwable cause) {
		super(message, cause);
	}
	/**{@inheritDoc}*/
	public VarNotRegisteredException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
