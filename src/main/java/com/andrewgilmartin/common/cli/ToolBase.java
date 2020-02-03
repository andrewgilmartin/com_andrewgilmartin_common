package com.andrewgilmartin.common.cli;

import com.andrewgilmartin.common.cli.ReflectiveCommandLineParser;
import com.andrewgilmartin.common.text.SimpleMessageFormat;
import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class ToolBase implements Tool, Runnable {

    static {
        if (!"false".equals(System.getProperty("log4j.defaultInitOverride", "false"))) {
            Properties p = new Properties();
            // p.put("log4j.debug", "true");
            p.put("log4j.rootLogger", "INFO,console");
            p.put("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
            p.put("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
            p.put("log4j.appender.console.layout.conversionPattern", "%d{ISO8601} %p %t %c{2} %m%n");
            PropertyConfigurator.configure(p);
        }
    }

    protected CommonLogger logger;

    @Override
    public void construct() throws Exception {
        // empty
    }

    @Override
    public void initialize() throws Exception {
        // empty
    }

    @Override
    public void terminate() throws Exception {
        // empty
    }

    @Override
    public void execute() throws Exception {
        // empty
    }

    /**
     * Use this run method if you have already set the bean's properties.
     */
    @Override
    public void run() {
        // initialize the tool
        logger = CommonLogger.getLogger(this.getClass());
        try {
            initialize();

            // make sure to run terminate on exit
            Thread terminateThread = new Thread(() -> {
                try {
                    terminate();
                } catch (Exception e) {
                    logger.error(e);
                }
            });
            Runtime.getRuntime().addShutdownHook(terminateThread);

            // run the tool
            execute();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Use this run method if you want to set the bean's properties from command
     * line options.
     */
    public void run(String... args) {
        try {
            this.construct();
        } catch (Exception e) {
            logger.error(e);
            System.exit(2);
        }
        // set the properties
        try {
            ReflectiveCommandLineParser parser = new ReflectiveCommandLineParser();
            parser.parse(this, args, 0);
        } catch (Exception e) {
            showError("error: {0}", e.getLocalizedMessage());
            showHelp();
            System.exit(1);
        }
        // continue
        run();
    }

    protected void showError(String message, Object... args) {
        System.err.println(SimpleMessageFormat.format(message, args));
    }

    protected void showHelp() {
        System.err.printf("usage: %s %s\n", this.getClass(), "...");
    }

    /**
     * In your sub-class duplicate this main() but replace the ToolBase instance
     * with your sub-class instance. While there are some means of knowing the
     * class context of the main() call they seen as hacks to some and magic to
     * others.
     */
    public static void main(String... args) throws Exception {
        throw new Exception("you need to define the main() method in your subclass");
        // ToolBase tool = new ToolBase();
        // tool.run(args);
    }
}

// END
