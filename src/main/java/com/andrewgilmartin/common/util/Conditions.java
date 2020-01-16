package com.andrewgilmartin.common.util;

import com.andrewgilmartin.common.exceptions.UnacceptableConditionException;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A collection of condition tests for use with pre- and post- conditions.
 * Conditions can not be disable.
 *
 * When enhancing this class with additional tests ensure that the test method
 * signature follows the convention {@code
 *
 * void isNAME( TEST|TRUTH|SUBJECT, CRITERIA, MESSAGE, MESSAGE-PARAMETERS ) throws UnacceptableConditionException
 *
 * }
 */
public class Conditions {

    public static void isEqual(Object a, Object b, String message, Object... parameters) throws UnacceptableConditionException {
        if (a != b && (a == null || !a.equals(b))) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEqual(Number a, Number b, String message, Object... parameters) throws UnacceptableConditionException {
        // compare strings because, for example Long and Integer can not be compared
        if (a != b && (a == null || !a.toString().equals(b.toString()))) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEqual(Object a, Object b, String message, Object... parameters) throws UnacceptableConditionException {
        if (a == b || (a != null && a.equals(b))) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isTrue(boolean test, String message, Object... parameters) throws UnacceptableConditionException {
        if (!test) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isTrue(AtomicBoolean test, String message, Object... parameters) throws UnacceptableConditionException {
        if (!test.get()) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isFalse(boolean test, String message, Object... parameters) throws UnacceptableConditionException {
        if (test) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isFalse(AtomicBoolean test, String message, Object... parameters) throws UnacceptableConditionException {
        if (test.get()) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotNull(Object subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (subject == null) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNull(Object subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (subject != null) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(Collection<?> subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (subject == null || subject.isEmpty()) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(Collection<?> subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (subject == null || !subject.isEmpty()) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(int[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array != null && array.length != 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(long[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array != null && array.length != 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(double[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array != null && array.length != 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(float[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array != null && array.length != 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(boolean[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array != null && array.length != 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(Object[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array != null && array.length != 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isEmpty(String subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (!StringUtils.isEmpty(subject)) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(String subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (StringUtils.isEmpty(subject)) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(Object subject, String message, Object... parameters) throws UnacceptableConditionException {
        if (StringUtils.isEmpty(subject)) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(int[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array == null || array.length == 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(long[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array == null || array.length == 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(double[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array == null || array.length == 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(float[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array == null || array.length == 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(boolean[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array == null || array.length == 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isNotEmpty(Object[] array, String message, Object... parameters) throws UnacceptableConditionException {
        if (array == null || array.length == 0) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isClass(Class<?> subjectClass, Class<?> expectedClass, String message, Object... parameters) throws UnacceptableConditionException {
        if (subjectClass != expectedClass) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isAssignable(Class<?> general, Class<?> special, String message, Object... parameters) throws UnacceptableConditionException {
        // If general is Number and special is Integer then java.lang.Number.class.isAssignableFrom( java.lang.Integer.class ) is true
        if (!general.isAssignableFrom(special)) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isFile(File element, String message, Object... parameters) {
        if (element == null || !element.isFile()) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isDirectory(File element, String message, Object... parameters) {
        if (element == null || !element.isDirectory()) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isBetween(int value, int min, int max, String message, Object... parameters) {
        if (value < min || value > max) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isBetween(long value, long min, long max, String message, Object... parameters) {
        if (value < min || value > max) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }

    public static void isBetween(double value, double min, double max, String message, Object... parameters) {
        if (value < min || value > max) {
            throw new UnacceptableConditionException(message, parameters);
        }
    }
}

// END
