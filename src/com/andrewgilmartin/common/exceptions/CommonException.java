package com.andrewgilmartin.common.exceptions;

public class CommonException extends Exception {
    
    private static final long serialVersionUID = 2942756967514156093L;

    protected Object[] parameters;

    protected CommonException( Throwable cause, String message, Object ... parameters ) {
        super( message, cause );
        this.parameters = parameters;
    }

    protected CommonException( Throwable cause ) {
        super( cause );
    }

    protected CommonException( String message, Object ... parameters ) {
        super( message );
        this.parameters = parameters;
    }

    protected CommonException() {
        super();
    }

    /**
     * Returns a fully formatted message using the message and parameters
     * passed during instance construction.
     */
    @Override
    public String getLocalizedMessage() {
        String message = getMessage();
        if ( message != null && parameters != null && parameters.length > 0 ) {
            try {
                return java.text.MessageFormat.format( message, parameters );
            }
            catch ( IllegalArgumentException e ) {
                e.printStackTrace();
            }
        }
        return message;
    }
 }

