package com.andrewgilmartin.common.exceptions;

public class CommonRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 641978799121212044L;
    
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

    /**
     * Returns a fully formatted message using the message and parameters
     * passed during instance construction.
     */
    @Override
    public String getLocalizedMessage() {
        String message = getMessage();
        if (message != null && parameters != null && parameters.length > 0) {
            try {
                return java.text.MessageFormat.format(message, parameters);
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return message;
    }
}
