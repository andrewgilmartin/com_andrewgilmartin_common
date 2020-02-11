package com.andrewgilmartin.common.text;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isWhitespace;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses a simple environment variables file suitable for Bash. The variables
 * must be one per line and have the form "name=value" or "name='value'".
 */
public class EnvironmentVariablesFile {

    /**
     * Set the System properties from the environment variables in the given
     * file.
     */
    public static void setProperies(File source) throws IOException {
        parse(source).forEach((k, v) -> System.setProperty(k, v));
    }

    /**
     * Set the System properties from the environment variables from the given
     * reader.
     */
    public static void setProperies(Reader source) throws IOException {
        parse(source).forEach((k, v) -> System.setProperty(k, v));
    }

    /**
     * Parse the environment variables in the given file.
     */
    public static Map<String, String> parse(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return parse(reader);
        }
    }

    /**
     * Parse the environment variables from the given reader.
     */
    public static Map<String, String> parse(Reader reader) throws IOException {
        Map<String, String> varables = new HashMap<>();
        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();

        int state = 0;
        PARSE:
        for (;;) {
            int c = reader.read();
            if (c == -1) {
                break PARSE;
            }
            switch (state) {
                case 0:
                    if (isAlphabetic(c)) {
                        name.append((char) c);
                        state = 1;
                    }
                    break;
                case 1:
                    if (c == '=') {
                        state = 2;
                    } else {
                        name.append((char) c);
                    }
                    break;
                case 2:
                    if (c == '\'') {
                        state = 3;
                    } else if (c == '\\') {
                        state = 12;
                    } else if (isWhitespace(c)) {
                        varables.put(name.toString(), value.toString());
                        name.setLength(0);
                        value.setLength(0);
                        state = 0;
                    } else {
                        value.append((char) c);
                    }
                    break;
                case 3:
                    if (c == '\'') {
                        state = 2;
                    } else if (c == '\'') {
                        state = 13;
                    } else {
                        value.append((char) c);
                    }
                    break;
                case 12:
                case 13:
                    if (c == 'n') {
                        value.append('\n');
                    } else if (c == 't') {
                        value.append('\t');
                    } else {
                        value.append((char) c);
                    }
                    state -= 10;
                    break;
            }
        }
        return varables;
    }

    public static void main(String... args) throws Exception {
        for (String filename : args) {
            try (FileReader reader = new FileReader(filename)) {
                System.out.println(filename);
                Map<String, String> p = EnvironmentVariablesFile.parse(reader);
                for (Map.Entry<String, String> e : p.entrySet()) {
                    System.out.println(e.getKey() + ": ~" + e.getValue() + "~");
                }
            }
        }
    }
}

// END
