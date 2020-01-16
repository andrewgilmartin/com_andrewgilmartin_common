package com.andrewgilmartin.common.mysql.autorecord;

import com.andrewgilmartin.common.exceptions.CommonRuntimeException;

public class AutoRecordException extends CommonRuntimeException {

    public AutoRecordException(String message, Object... parameters) {
        super(message, parameters);
    }

    public AutoRecordException(Throwable cause, String message, Object... parameters) {
        super(cause, message, parameters);
    }

    public AutoRecordException(Throwable cause) {
        super(cause);
    }
}

// END