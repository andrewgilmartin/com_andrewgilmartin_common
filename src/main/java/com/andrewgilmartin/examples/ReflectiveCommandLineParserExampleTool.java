package com.andrewgilmartin.examples;

import com.andrewgilmartin.common.util.ReflectiveCommandLineParser;
import com.andrewgilmartin.common.util.logger.CommonLogger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Pattern;

public class ReflectiveCommandLineParserExampleTool implements Runnable {

    private static final CommonLogger logger = CommonLogger.getLogger(ReflectiveCommandLineParserExampleTool.class);

    public void showHelp() {
        logger.info("usage: {0} "
                + "[--flagA] "
                + "[--optionA string] "
                + "[--optionB string long] "
                + "[--optionC boolean] "
                + "[--optionD regular-expression] "
                + "[--optionE YYYY-MM-DD] "
                + "[--optionF YYYY-MM-DD\\ HH:MM:SS] "
                + "[--help] "
                + "( string long )...", this.getClass());
        Runtime.getRuntime().exit(1);
    }

    public void setFlagA() {
        logger.info("setFlagA");
    }

    public void setOptionA(String positional1) {
        logger.info("setOptionA {0}", positional1);
    }

    public void setOptionB(String positional1, Long positional2) {
        logger.info("setOptionB {0} {1}", positional1, positional2);
    }

    public void addOptionC(Boolean positional1) {
        logger.info("addOptionC {0}", positional1);
    }

    public void addOptionD(Pattern positional1) {
        logger.info("addOptionD {0}", positional1);
    }

    public void addOptionE(Date positional1) {
        logger.info("addOptionE {0}", positional1);
    }

    public void addOptionF(Timestamp positional1) {
        logger.info("addOptionF {0}", positional1);
    }

//    public void addPositional(String positional) {
//        logger.info("addPositional {0}", positional);
//    }
    
//    public void setPositional(String positional, Long positonal2 ) {
//        logger.info("setPositional {0} {1}", positional, positonal2);
//    }

    public void addPositional(String positional, Long positonal2) {
        logger.info("addPositional {0} {1}", positional, positonal2);
    }

    public void run() {
        logger.info("run");
    }

    public static void main(String[] args) throws Exception {
        ReflectiveCommandLineParser parser = new ReflectiveCommandLineParser();
        parser.run(new ReflectiveCommandLineParserExampleTool(), args);
    }
}

// END
