package io.github.mianalysis.mia.process.exceptions;

public class LongOverflowException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = -1673773320914088818L;

    public LongOverflowException(String errorMessage) {
        super(errorMessage);
    }
}
