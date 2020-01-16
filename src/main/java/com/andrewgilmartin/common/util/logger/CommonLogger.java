package com.andrewgilmartin.common.util.logger;

import com.andrewgilmartin.common.text.SimpleMessageFormat;
import org.apache.log4j.Level;

/**
 * A logger that builds upon Log4j and MessageFormat.
 */
public final class CommonLogger {

    private org.apache.log4j.Logger logger;

    public static CommonLogger getLogger(String name) {
        return new CommonLogger(org.apache.log4j.Logger.getLogger(name));
    }

    public static CommonLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    private CommonLogger(org.apache.log4j.Logger logger) {
        this.logger = logger;
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String messageFormat, Object... messageParameters) {
        if (Level.DEBUG.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.debug(format(messageFormat, messageParameters));
        }
    }

    public void debug(Throwable cause, String messageFormat, Object... messageParameters) {
        if (Level.DEBUG.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.debug(format(messageFormat, messageParameters), cause);
        }
    }

    public void debug(Throwable cause) {
        if (Level.DEBUG.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.debug(cause.getLocalizedMessage(), cause);
        }
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void trace(String messageFormat, Object... messageParameters) {
        if (Level.TRACE.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.trace(format(messageFormat, messageParameters));
        }
    }

    public void trace(Throwable cause, String messageFormat, Object... messageParameters) {
        if (Level.TRACE.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.trace(format(messageFormat, messageParameters), cause);
        }
    }

    public void info(String messageFormat, Object... messageParameters) {
        if (Level.INFO.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.info(format(messageFormat, messageParameters));
        }
    }

    public void info(Throwable cause, String messageFormat, Object... messageParameters) {
        if (Level.INFO.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.info(format(messageFormat, messageParameters), cause);
        }
    }

    public void info(Throwable cause) {
        if (Level.INFO.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.info(cause.getLocalizedMessage(), cause);
        }
    }

    public void warn(String messageFormat, Object... messageParameters) {
        if (Level.WARN.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.warn(format(messageFormat, messageParameters));
        }
    }

    public void warn(Throwable cause, String messageFormat, Object... messageParameters) {
        if (Level.WARN.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.warn(format(messageFormat, messageParameters), cause);
        }
    }

    public void warn(Throwable cause) {
        if (Level.WARN.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.warn(cause.getLocalizedMessage(), cause);
        }
    }

    public void error(String messageFormat, Object... messageParameters) {
        if (Level.ERROR.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.error(format(messageFormat, messageParameters));
        }
    }

    public void error(Throwable cause, String messageFormat, Object... messageParameters) {
        if (Level.ERROR.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.error(format(messageFormat, messageParameters), cause);
        }
    }

    public void error(Throwable cause) {
        if (Level.ERROR.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.error(cause.getLocalizedMessage(), cause);
        }
    }

    public void fatal(String messageFormat, Object... messageParameters) {
        if (Level.FATAL.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.fatal(format(messageFormat, messageParameters));
        }
    }

    public void fatal(Throwable cause, String messageFormat, Object... messageParameters) {
        if (Level.FATAL.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.fatal(format(messageFormat, messageParameters), cause);
        }
    }

    public void fatal(Throwable cause) {
        if (Level.FATAL.isGreaterOrEqual(logger.getEffectiveLevel())) {
            logger.fatal(cause.getMessage(), cause);
        }
    }

    protected String format(String messageFormat, Object[] messageParameters) {
        // String message = MessageFormat.format(messageFormat, messageParameters);
        String message = SimpleMessageFormat.format(messageFormat,messageParameters);
        return message;
    }
}

// END
