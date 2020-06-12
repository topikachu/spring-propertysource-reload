package io.github.topikachu.properties.reload;

public class ReloadableException extends RuntimeException {
	public ReloadableException() {
	}

	public ReloadableException(String message) {
		super(message);
	}

	public ReloadableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReloadableException(Throwable cause) {
		super(cause);
	}

	public ReloadableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
