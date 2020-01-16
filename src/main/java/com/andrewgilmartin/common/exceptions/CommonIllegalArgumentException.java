package com.andrewgilmartin.common.exceptions;

public class CommonIllegalArgumentException extends IllegalArgumentException {

    protected Object[] parameters;

    public CommonIllegalArgumentException(Throwable cause, String message, Object... parameters) {
        super(message, cause);
        this.parameters = parameters;
    }

    public CommonIllegalArgumentException(Throwable cause) {
        super(cause);
    }

    public CommonIllegalArgumentException(String message, Object... parameters) {
        super(message);
        this.parameters = parameters;
    }

    @Override
    public String getLocalizedMessage() {
        return ExceptionUtils.getLocalizedMessage(getMessage(), parameters);
    }
}

// END