package quva.core;

public class EquationFormatException extends QuvaException {
	private static final long serialVersionUID = 7271348586367397305L;

	public EquationFormatException() {
	}

	public EquationFormatException(String message) {
		super(message);
	}

	public EquationFormatException(Throwable cause) {
		super(cause);
	}

	public EquationFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public EquationFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
