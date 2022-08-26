package quva.util;

/**This exception only gets thrown if {@code QuvaProgram.launch} tries to create a non-QUBOMatrix
 * @see quva.util.QuvaProgram#launch(String, Object[])*/
public class NoQUBOMatrixException extends RuntimeException {
	private static final long serialVersionUID = -3778101907703860333L;

	/**{@inheritDoc}*/
	public NoQUBOMatrixException() {
	}

	/**{@inheritDoc}*/
	public NoQUBOMatrixException(String message) {
		super(message);
	}

	/**{@inheritDoc}*/
	public NoQUBOMatrixException(Throwable cause) {
		super(cause);
	}

	/**{@inheritDoc}*/
	public NoQUBOMatrixException(String message, Throwable cause) {
		super(message, cause);
	}

	/**{@inheritDoc}*/
	public NoQUBOMatrixException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
