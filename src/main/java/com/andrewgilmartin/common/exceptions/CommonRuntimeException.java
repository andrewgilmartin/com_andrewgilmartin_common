package com.andrewgilmartin.common.exceptions;

public class CommonRuntimeException extends RuntimeException {

    protected Object[] parameters;

    protected CommonRuntimeException(Throwable cause, String message, Object... parameters) {
        super(message, cause);
        this.parameters = parameters;
    }

    protected CommonRuntimeException(String message, Object... parameters) {
        super(message);
        this.parameters = parameters;
    }

    protected CommonRuntimeException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return ExceptionUtils.getLocalizedMessage(getMessage(), parameters);
    }
}
