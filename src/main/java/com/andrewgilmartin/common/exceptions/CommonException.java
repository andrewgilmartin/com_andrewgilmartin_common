package com.andrewgilmartin.common.exceptions;

public class CommonException extends Exception {
    
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

    @Override
    public String getLocalizedMessage() {
        return ExceptionUtils.getLocalizedMessage(getMessage(), parameters);
    }
 }

