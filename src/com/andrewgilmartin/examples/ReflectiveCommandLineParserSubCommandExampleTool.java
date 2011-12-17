package com.andrewgilmartin.examples;

import com.andrewgilmartin.common.util.ReflectiveCommandLineParser;
import com.andrewgilmartin.common.util.logger.CommonLogger;

public class ReflectiveCommandLineParserSubCommandExampleTool {

    private static final CommonLogger logger = CommonLogger.getLogger(ReflectiveCommandLineParserSubCommandExampleTool.class);

    class SubCommand1 implements Runnable {

        public void setFlag1() {
            logger.info("SubCommand1.setFlag1");
        }

        public void run() {
            logger.info("SubCommand1.run");
        }
    }

    class SubCommand2 implements Runnable {

        public void setFlag2() {
            logger.info("SubCommand2.setFlag2");
        }

        public void setFlag4() {
            // note that this method overrides the one in the outter class.
            logger.info("SubCommand2.setFlag4");
        }

        public void run() {
            logger.info("SubCommand2.run");
        }
    }

    public void setFlag3() {
        logger.info("ExampleSubCommandTool.setFlag3");
    }

    public void setFlag4() {
        logger.info("ExampleSubCommandTool.setFlag4");
    }

    public void showHelp() {
        logger.info("usage: {0}"
                + " ( sub1 [ --flag1 ] [ --flag3 ] [ --flag4 ] )"
                + " ( sub2 [ --flag2 ] [ --flag3 ] [ --flag4 ] )",this.getClass());
    }

    public void run(String[] args) throws Exception {
        if (args.length > 0) {
            ReflectiveCommandLineParser parser = new ReflectiveCommandLineParser();
            String subCommandName = args[0];
            if ("sub1".equals(subCommandName)) {
                parser.run(new SubCommand1(), args,1);
            }
            else if ("sub2".equals(subCommandName)) {
                parser.run(new SubCommand2(), args,1);
            }
            else {
                showHelp();
            }
        }
        else {
            showHelp();
        }
    }

    public static void main(String[] args) throws Exception {
        ReflectiveCommandLineParserSubCommandExampleTool tool = new ReflectiveCommandLineParserSubCommandExampleTool();
        tool.run(args);
    }
}

// END
