package batch.config.classes;

public class NotFoundNameException extends RuntimeException {

	public NotFoundNameException() {
	}

	public NotFoundNameException(String message) {
		super(message);
	}

	public NotFoundNameException(Throwable cause) {
		super(cause);
	}

	public NotFoundNameException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundNameException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
    
    
}
