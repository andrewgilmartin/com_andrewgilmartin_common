package com.andrewgilmartin.common.util;

public class StringUtils {

    /**
     * Returns true if the given string is null, is zero length, or is composed
     * of only whitespace characters.
     */
    public static boolean isEmpty(String s) {
        // this implementation is much faster than s.trim().length() == 0
        if (s != null) {
            for (int l = s.length(), i = 0; i < l; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the given object toString() method is null, is zero
     * length, or is composed of only whitespace characters.
     */
    public static boolean isEmpty(Object o) {
        return o == null || isEmpty(o.toString());
    }

    /**
     * Returns true if the given string is not null, is not zero length, and is
     * not composed of only whitespace characters.
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * Returns true if the given string is not null, is not zero length, and is
     * not composed of only whitespace characters.
     */
    public static boolean isNotEmpty(Object o) {
        return !isEmpty(o);
    }

}
