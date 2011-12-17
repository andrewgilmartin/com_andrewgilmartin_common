package com.andrewgilmartin.common.util;

/**
 * ReflectiveCommandLineParser by Andrew Gilmartin (andrew@andrewgilmartin.com)
 * is licensed under a Creative Commons Attribution 3.0 Unported License.
 * For more information see http://creativecommons.org/licenses/by/3.0/
 */

import com.andrewgilmartin.common.lang.LangUtils;
import com.andrewgilmartin.common.lang.MethodRunner;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * The reflective command line tool uses reflection to transform a command line
 * into a series of method calls for configuring the given tool and afterwards
 * running it. For example, the method "void setVerbose()" will be called when
 * the command line option "--verbose" is found, the method
 * "void setRe( Pattern pattern )" is called when "--re 'foo.*bar'" is found,
 * and "void setDatabase( URI jdbcUri, String user, String password )" is called
 * when "--database jdbc:mysql://localhost/xx root r00t" is found. Note that
 * the method names can begin with "set" or "add", eg "addDatabase".
 *
 * When there are positional arguments a "setPositional" or an "addPositional"
 * method is is looked for. The positional method may take more than one
 * parameter. If the first positional argument looks like an option then preceed
 * it with the "--" marker that (optional) separates options from positional
 * arguments.
 *
 * Positional and option arguments are automatically converted from their
 * textual form to their object form. For the common types this class knows
 * how to convert the value from text to its typed instance. The common types
 * are
 *
 * <ul>
 * <li>java.io.File
 * <li>java.lang.Boolean
 * <li>java.lang.Character
 * <li>java.lang.Class
 * <li>java.lang.Double
 * <li>java.lang.Float
 * <li>java.lang.Integer
 * <li>java.lang.Long
 * <li>java.lang.String
 * <li>java.math.BigDecimal
 * <li>java.math.BigInteger
 * <li>java.net.URI
 * <li>java.sql.Timestamp (in 'YYYY-MM-DD HH:MM:SS' format)
 * <li>java.util.Date (in 'YYYY-MM-DD' format)
 * <li>java.util.UUID
 * <li>java.util.regex.Pattern
 * </ul>
 *
 * For all others an appropriate java.beans.PropertyEditor is used.
 *
 * @see PropertyEditor
 * @see PropertyEditorManager
 */
public class ReflectiveCommandLineParser {

    /**
     * Parse the given arguments against the methods of the given tool. Start
     * parsing the arguments array at offset zero. Once parsed run the tool.
     */
    public void run(Runnable tool, String[] arguments) throws Exception {
        parse(tool, arguments, 0);
        tool.run();
    }

    /**
     * Parse the given arguments against the methods of the given tool. Start
     * parsing the arguments array at the given offset. Once parsed run the tool.
     */
    public void run(Runnable tool, String[] arguments, int argumentsOffset) throws Exception {
        parse(tool, arguments, argumentsOffset);
        tool.run();
    }

    /**
     * Parse the given arguments against the methods of the given tool. Start
     * parsing the arguments array at the given offset.
     */
    public void parse(Object tool, String[] arguments, int argumentsOffset) throws Exception {
        MethodRunner helpRunnner = findMethodRunner(tool, "showHelp");
        // keep track of the setters used as they must only be used once
        Set<MethodRunner> usedSetters = new HashSet<MethodRunner>();
        int argumentIndex = argumentsOffset;
        // parse the command line options
        while (argumentIndex < arguments.length) {
            String argument = arguments[argumentIndex++];
            if ("--".equals(argument)) {
                // done parsing the options at this marker
                break;
            }
            else if (argument.startsWith("--")) {
                // which option is being used?
                String optionName = argument.substring(2);
                MethodRunner optionRunner = "help".equals(optionName) ? helpRunnner : getOptionMethodRunner(tool, optionName);
                if (optionRunner != null) {
                    // has the setter option be used before?
                    if (optionRunner.getName().startsWith("set")) {
                        if (usedSetters.contains(optionRunner)) {
                            throw new Exception("command line option '--" + optionName + "' must only be used once");
                        }
                        usedSetters.add(optionRunner);
                    }
                    // is it a "flag" option, ie no arguments
                    if (optionRunner.getMethod().getParameterTypes().length == 0) {
                        // call the option method
                        optionRunner.run();
                    }
                    // are there enough arguments for the option
                    else if (argumentIndex + optionRunner.getParameterTypes().length - 1 < arguments.length) {
                        // convert the arguments from text to objects
                        Object[] parameterValues = new Object[optionRunner.getParameterTypes().length];
                        for (int p = 0; p < optionRunner.getParameterTypes().length; p++) {
                            parameterValues[p] = convertArgumentValue(optionRunner.getParameterTypes()[p], arguments[argumentIndex++]);
                        }
                        // call the option method
                        optionRunner.run(parameterValues);
                    }
                    else {
                        throw new Exception("command line option '--" + optionName + "' has too few arguments");
                    }
                }
                else {
                    throw new Exception("unknown command line option '--" + optionName + "'");
                }
            }
            else {
                // must be at the end of option parsing
                argumentIndex--; // make sure to reprocess this argument
                break;
            }
        }
        // parse the positonal arguments
        MethodRunner positionalRunner = getOptionMethodRunner(tool, "positional");
        if (positionalRunner != null) {
            while (argumentIndex < arguments.length) {
                // has the positional setter option be used before?
                if (positionalRunner.getName().startsWith("set")) {
                    if (usedSetters.contains(positionalRunner)) {
                        throw new Exception("positional command line arguments must only be used once");
                    }
                    usedSetters.add(positionalRunner);
                }
                // are there enough positional arguments for the method?
                if (argumentIndex + positionalRunner.getParameterTypes().length - 1 < arguments.length) {
                    // convert the arguments from text to objects
                    Object[] parameterValues = new Object[positionalRunner.getParameterTypes().length];
                    for (int p = 0; p < positionalRunner.getParameterTypes().length; p++) {
                        parameterValues[p] = convertArgumentValue(positionalRunner.getParameterTypes()[p], arguments[argumentIndex++]);
                    }
                    // call the positional method
                    positionalRunner.run(parameterValues);
                }
                else {
                    throw new Exception("too few positional command line arguments");
                }
            }
        }
        // are all the arguments used?
        if (argumentIndex < arguments.length) {
            throw new Exception("too many positional command line arguments");
        }
        // done
    }

    /**
     * Return the adder or setter methodist with the given name. Option name
     * must not include any "--" prefix. It is assumed that, for example, the
     * option name "fooBar" maps to the "addFooBar" or "setFooBar" method names.
     */
    protected MethodRunner getOptionMethodRunner(Object instance, String optionName) throws NoSuchMethodException, IllegalAccessException {
        MethodRunner m = findMethodRunner(instance, "set" + optionName.substring(0, 1).toUpperCase() + optionName.substring(1));
        if (m == null) {
            m = findMethodRunner(instance, "add" + optionName.substring(0, 1).toUpperCase() + optionName.substring(1));
        }
        return m;
    }

    /**
     * Find the given method with in the given instance or any of its outer
     * class instances. That is, if A is an inner-class of B and B is an inner-
     * class of C then A, B, and C will be search for the method name.
     */
    protected MethodRunner findMethodRunner(Object instance, String methodName) throws NoSuchMethodException, IllegalAccessException {
        while (instance != null) {
            for (Method method : instance.getClass().getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return new MethodRunner(instance, method);
                }
            }
            try {
                Field this$0 = instance.getClass().getDeclaredField("this$0");
                instance = LangUtils.get(this$0, instance);
            }
            catch (NoSuchFieldException e) {
                instance = null;
            }
        }
        return null;
    }

    /**
     * Return the object value for the given textual representation. If the
     * value can not be converted then null is returned.
     */
    protected Object convertArgumentValue(Class type, String raw) throws ParseException, ClassNotFoundException {
        if (type == String.class) {
            return raw;
        }
        if (type == Character.class) {
            return Character.valueOf(raw.charAt(0));
        }
        if (type == Long.class) {
            return Long.valueOf(raw);
        }
        if (type == Integer.class) {
            return Integer.valueOf(raw);
        }
        if (type == Float.class) {
            return Float.valueOf(raw);
        }
        if (type == Double.class) {
            return Double.valueOf(raw);
        }
        if (type == java.math.BigInteger.class) {
            return new java.math.BigInteger(raw);
        }
        if (type == java.math.BigDecimal.class) {
            return new java.math.BigDecimal(raw);
        }
        if (type == Boolean.class) {
            return Boolean.valueOf(raw);
        }
        if (type == java.sql.Timestamp.class) {
            return java.sql.Timestamp.valueOf(raw);
        }
        if (type == java.util.Date.class) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            return fmt.parse(raw);
        }
        if (type == java.net.URI.class) {
            return java.net.URI.create(raw);
        }
        if (type == java.util.UUID.class) {
            return java.util.UUID.fromString(raw);
        }
        if (type == java.util.regex.Pattern.class) {
            return java.util.regex.Pattern.compile(raw);
        }
        if (type == java.io.File.class) {
            return new java.io.File(raw);
        }
        if (type == java.lang.Class.class) {
            return java.lang.Class.forName(raw);
        }
        PropertyEditor editor = PropertyEditorManager.findEditor(type);
        if (editor != null) {
            editor.setAsText(raw);
            return editor.getValue();
        }
        return null;
    }
}

// END
