package com.andrewgilmartin.examples;

/**
 * ListFiles by Andrew Gilmartin (andrew@andrewgilmartin.com) is licensed under
 * a Creative Commons Attribution 3.0 Unported License.
 * For more information see http://creativecommons.org/licenses/by/3.0/
 */

import com.andrewgilmartin.common.util.ReflectiveCommandLineParser;
import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An example of using the ReflectiveCommandLineParser class. It lists the
 * file in the given directories at (optionally) match one of the given
 * patterns/regular-expressions. For example, show all the files that
 * match i.d or ^c within the current directory and the global temporary
 * directory:
 *
 * com.andrewgilmartin.examples.ListFiles --pattern i.d --pattern ^c ./ /tmp/
 *
 * @see ReflectiveCommandLineParser
 */
public class ListFiles implements Runnable {

    private static final CommonLogger logger = CommonLogger.getLogger(ListFiles.class);

    private PrintStream out = System.out;
    private boolean detail = false;
    private List<Pattern> patterns = new LinkedList<Pattern>();
    private List<File> directories = new LinkedList<File>();

    public void showHelp() {
        System.err.printf("usage: %s "
                + "[--detail] "
                + "[--pattern regex]... "
                + "[--output filename] "
                + "directory...\n", ListFiles.class);
        System.exit(1);
    }

    public void setDetail() {
        detail = true;
    }

    public void addPattern(Pattern pattern) {
        patterns.add(pattern);
    }

    public void setOutput(File file) throws FileNotFoundException {
        out = new PrintStream(new FileOutputStream(file));
    }

    public void addPositional(File directory) {
        directories.add(directory);
    }

    public void run() {
        try {
            for (File directory : directories) {
                if (directory.exists()) {
                    for (File element : directory.listFiles()) {
                        boolean match = false;
                        if (!patterns.isEmpty()) {
                            for (Pattern pattern : patterns) {
                                if (pattern.matcher(element.getName()).find()) {
                                    match = true;
                                }
                            }
                        }
                        else {
                            match = true;
                        }
                        if (match) {
                            if (detail) {
                                out.printf("%s ... details ...\n", element.getCanonicalPath());
                            }
                            else {
                                out.printf("%s\n", element.getCanonicalPath());
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            logger.error(e);
        }
        finally {
            out.close();
        }
    }

    public static void main(String[] args) throws Throwable {
        ReflectiveCommandLineParser parser = new ReflectiveCommandLineParser();
        parser.run(new ListFiles(), args);
    }
}

// END
