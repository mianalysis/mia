package io.github.mianalysis.mia.module.inputoutput;

public class FrameOutOfRangeException extends RuntimeException {
    
    /**
     *
     */
    private static final long serialVersionUID = -5409076953209909970L;

    public FrameOutOfRangeException() {
    }

    public FrameOutOfRangeException(String message) {
        super(message);
    }

    public FrameOutOfRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameOutOfRangeException(Throwable cause) {
        super(cause);
    }

    public FrameOutOfRangeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}