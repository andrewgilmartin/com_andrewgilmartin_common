package com.andrewgilmartin.common.lang;

import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Associates an object and method for invoking at a later time.
 */
public class MethodRunner {

    private static final CommonLogger logger = CommonLogger.getLogger(MethodRunner.class);

    private Object object;
    private Method method;

    public MethodRunner(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    /**
     * Returns the method's name.
     */
    public String getName() {
        return method.getName();
    }

    /**
     * Does the method take any parameters.
     */
    public boolean hasParameters() {
        return method.getParameterTypes().length != 0;
    }

    /**
     * Returns the method's parameter types.
     */
    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    /**
     * Returns the method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the object (on which the method will be invoked).
     */
    public Object getObject() {
        return object;
    }

    /**
     * Call the configured method on the configured object with the given
     * parameters.
     */
    public <T> T run(Object... parameters) {
        try {
            T result = (T) LangUtils.invoke(method, object, parameters);
            return result;
        }
        catch (IllegalAccessException e) {
            logger.error(e);
        }
        catch (IllegalArgumentException e) {
            logger.error(e);
        }
        catch (InvocationTargetException e) {
            logger.error(e);
        }
        return null;
    }
}

// END
