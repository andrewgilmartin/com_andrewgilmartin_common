package com.andrewgilmartin.common.text;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.helpers.LogLog;

/**
 * This class works much like {@link MessageFormat}. However, it does not use
 * the locale for formatting but instead uses simple patterns for numbers, etc.
 * This formatter is mostly suitable for logging and other non-locale specific
 * output.
 */
public class SimpleMessageFormat {

    // constants
    private static final Pattern STRING_STYLE = Pattern.compile("(\\d+)?((:|\\.\\.)(\\d+))?"); // [offset][:length] or [start][..end]
    // configured
    private final String pattern;

    public SimpleMessageFormat(String pattern) {
        this.pattern = pattern;
    }

    public String format(Object... values) {
        return format(pattern, values);
    }

    public static String format(String pattern, Object... values) {
        int state = 0;
        int length = pattern.length();
        int cursor = 0;
        StringBuilder text = new StringBuilder();
        int index = 0;
        StringBuilder type = null;
        StringBuilder style = null;
        LOOP: 
        for (;;) {
            if (state < 0) {
                if (values.length != 0 && index < values.length) {
                    format(text, values[index], type, style);
                }
                else {
                    // TODO this situation is really an error and so don't allow it.
                    text.append("{").append(index).append("}");
                }
                index = 0;
                type = null;
                style = null;
                state = 0;
            }
            int c = cursor < length ? pattern.charAt(cursor) : -1;
            cursor += 1;
            switch (state) {
                case 0:
                    if (c == '{') {
                        state = 1;
                    }
                    else if (c == '\'') {
                        state = 5;
                    }
                    else if (c == -1) {
                        break LOOP;
                    }
                    else {
                        text.append((char) c);
                    }
                    break;
                case 1:
                    if ('0' <= c && c <= '9') {
                        index = c - '0';
                        state = 2;
                    }
                    else {
                        throw new IllegalArgumentException("misformed pattern: pattern=" + pattern);
                    }
                    break;
                case 2: // argument index
                    if ('0' <= c && c <= '9') {
                        index = index * 10 + (c - '0');
                        state = 2;
                    }
                    else if (c == '}') {
                        state = -1;
                    }
                    else if (c == ',') {
                        type = new StringBuilder();
                        state = 3;
                    }
                    else {
                        throw new IllegalArgumentException("misformed pattern: pattern=" + pattern);
                    }
                    break;
                case 3: // argument type
                    if (c == '}') {
                        state = -1;
                    }
                    else if (c == ',') {
                        style = new StringBuilder();
                        state = 4;
                    }
                    else if (c == -1) {
                        throw new IllegalArgumentException("misformed pattern: pattern=" + pattern);
                    }
                    else {
                        type.append((char) c);
                    }
                    break;
                case 4: // argument style
                    if (c == '}') {
                        state = -1;
                    }
                    else if (c == -1) {
                        throw new IllegalArgumentException("misformed pattern: pattern=" + pattern);
                    }
                    else {
                        style.append((char) c);
                    }
                    break;
                case 5: // escaped apostrophe or quoted string
                    if (c == '\'') {
                        // escaped apostrophe
                        text.append('\'');
                        state = 0;
                    }
                    else if (c == -1) {
                        break LOOP;
                    }
                    else {
                        // quoted string
                        text.append((char) c);
                        state = 6;
                    }
                    break;
                case 6: // quoted string
                    if (c == -1) {
                        break LOOP;
                    }
                    else if (c == '\'') {
                        state = 0;
                    }
                    else {
                        text.append((char) c);
                    }
                    break;
                default:
                    throw new IllegalStateException("unhandled state: state=" + state);
            }
        }
        return text.toString();
    }

    private static StringBuilder format(StringBuilder text, Object value, StringBuilder name, StringBuilder pattern) {
        return format(text, value, name != null ? name.toString() : null, pattern != null ? pattern.toString() : null);
    }

    private static StringBuilder format(StringBuilder text, Object value, String type, String style) {
        if (value == null) {
            text.append("null");
        }
        else if (type == null) {
            Class<?> valueClass = value.getClass();
            if (valueClass == Integer.class) {
                Format f = new DecimalFormat("#");
                text.append(f.format(value));
            }
            else if (valueClass == Long.class) {
                Format f = new DecimalFormat("#");
                text.append(f.format(value));
            }
            else if (valueClass == Float.class) {
                if (((Float) value).isNaN()) {
                    text.append("NaN");
                }
                else {
                    Format f = new DecimalFormat("#.#");
                    text.append(f.format(value));
                }
            }
            else if (valueClass == Double.class) {
                if (((Double) value).isNaN()) {
                    text.append("NaN");
                }
                else {
                    Format f = new DecimalFormat("#.#");
                    text.append(f.format(value));
                }
            }
            else if (value instanceof Number) {
                text.append(value.toString());
            }
            else if (value instanceof Timestamp) {
                Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
                text.append(f.format(value));
            }
            else if (value instanceof Date) {
                Format f = new SimpleDateFormat("yyyy-MM-dd");
                text.append(f.format(value));
            }
            else if (value instanceof Exception) {
                Exception e = (Exception) value;
                StringWriter w = new StringWriter();
                e.printStackTrace(new PrintWriter(w));
                text.append(w.toString());
            }
            else {
                text.append(value.toString());
            }
        }
        else if ("date".equals(type)) {
            Format f = new SimpleDateFormat(style);
            text.append(f.format(value));
        }
        else if ("number".equals(type)) {
            Format f = new DecimalFormat(style);
            text.append(f.format(value));
        }
        else if ("time".equals(type)) {
            Format f = new SimpleDateFormat(style);
            text.append(f.format(value));
        }
        else if ("json".equals(type)) {
            try {
                throw new IllegalStateException("Add reflection based JSON encoding here");
            }
            catch (Exception e) {
                LogLog.error("unable to format to JSON", e);
            }
        }
        else if ("string".equals(type)) {
            Matcher m = STRING_STYLE.matcher(style);
            if (m.matches()) {
                String v = value.toString();
                int start = m.group(1) == null ? 0 : Integer.parseInt(m.group(1)); // start is an index
                int end
                    = m.group(4) == null
                        ? v.length() // no end value therefore use length
                        : ":".equals(m.group(3)) // ie, end is a length
                            ? start + Integer.parseInt(m.group(4)) // end is a length
                            : Integer.parseInt(m.group(4)) + 1; // end is an index
                if (end > v.length()) {
                    end = v.length();
                }
                if (start > end) {
                    start = end;
                }
                if (start > 0) {
                    text.append("[...]");
                }
                text.append(value.toString().subSequence(start, end));
                if (end < v.length()) {
                    text.append("[...]");
                }
            }
            else {
                LogLog.error("unable to use string style " + style);
            }
        }
        else {
            throw new IllegalArgumentException("unsupported format type: format-type=" + type);
        }
        return text;
    }
}

// END
