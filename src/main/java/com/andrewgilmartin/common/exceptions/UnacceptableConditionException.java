package com.andrewgilmartin.common.exceptions;

public class UnacceptableConditionException extends CommonIllegalStateException {

    public UnacceptableConditionException(Throwable cause, String message, Object... parameters) {
        super(cause, message, parameters);
    }

    public UnacceptableConditionException(Throwable cause) {
        super(cause);
    }

    public UnacceptableConditionException(String message, Object... parameters) {
        super(message, parameters);
    }
}

// END