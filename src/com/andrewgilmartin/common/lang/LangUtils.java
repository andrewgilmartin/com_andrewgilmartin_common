package com.andrewgilmartin.common.lang;

import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LangUtils {

    /**
     * Use relection to invoke a close() method on the given object. Log
     * any exceptions to the given logger.
     */
    public static void close(Object o, CommonLogger logger) {
        try {
            Method m = o.getClass().getMethod("close");
            if (m != null && m.getParameterTypes().length == 0) {
                try {
                    m.invoke(o);
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
            }
        }
        catch (NoSuchMethodException e) {
            // empty
        }
        catch (SecurityException e) {
            // empty
        }
    }

    /**
     * Return the field's value. Ensures that the field is accessible before
     * accessing.
     */
    public static <T> T get(Field field, Object object) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true); // make sure we can read the value
            return (T) field.get(object);
        }
        finally {
            field.setAccessible(accessible);
        }
    }

    /**
     * Invoke the method and return the result. Ensures that the method is
     * accessible before accessing.
     */
    public static <T> T invoke(Method method, Object object, Object... parameters) throws IllegalAccessException, InvocationTargetException {
        boolean accessible = method.isAccessible();
        try {
            method.setAccessible(true); // make sure we can call the method
            return (T) method.invoke(object,parameters);
        }
        finally {
            method.setAccessible(accessible);
        }
    }
}

// END
