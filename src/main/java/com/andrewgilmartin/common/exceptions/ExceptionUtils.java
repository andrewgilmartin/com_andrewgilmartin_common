package com.andrewgilmartin.common.exceptions;

import com.andrewgilmartin.common.text.SimpleMessageFormat;

public class ExceptionUtils {

    /**
     * Returns a fully formatted message using the message template and
     * parameters.
     */
    public static String getLocalizedMessage(String template, Object... parameters) {
        if (template != null && parameters != null && parameters.length > 0) {
            try {
                return SimpleMessageFormat.format(template, parameters);
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return template;
    }

    public static boolean hasCause(Throwable t, Class<? extends Throwable> cause) {
        for (Throwable c = t.getCause(); c != null; c = c.getCause()) {
            if (cause.isAssignableFrom(c.getClass())) {
                return true;
            }
        }
        return false;
    }

    public static <T extends Throwable> T findCause(Throwable t, Class<T> cause) {
        for (Throwable c = t.getCause(); c != null; c = c.getCause()) {
            if (cause.isAssignableFrom(c.getClass())) {
                return (T) c;
            }
        }
        return null;
    }

}

// END
