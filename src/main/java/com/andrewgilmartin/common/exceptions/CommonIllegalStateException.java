package com.andrewgilmartin.common.exceptions;

public class CommonIllegalStateException extends IllegalStateException {

    protected Object[] parameters;

    public CommonIllegalStateException(Throwable cause, String message, Object... parameters) {
        super(message, cause);
        this.parameters = parameters;
    }

    public CommonIllegalStateException(Throwable cause) {
        super(cause);
    }

    public CommonIllegalStateException(String message, Object... parameters) {
        super(message);
        this.parameters = parameters;
    }

    @Override
    public String getLocalizedMessage() {
        return ExceptionUtils.getLocalizedMessage(getMessage(), parameters);
    }
}

// END