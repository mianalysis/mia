package io.github.mianalysis.mia.process.exceptions;

public class IntegerOverflowException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = -4266675918342929017L;

    public IntegerOverflowException() {
    }

    public IntegerOverflowException(String message) {
        super(message);
    }

    public IntegerOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegerOverflowException(Throwable cause) {
        super(cause);
    }

    public IntegerOverflowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
