package com.andrewgilmartin.common.util;

public interface Tool {

    /**
     * Implement the construction operations here. This method is called before
     * any command line arguments are handled. It is a good place to override or
     * otherwise prepare for command line parsing. Throw a runtime exception if
     * you need to abort.
     */
    void construct() throws Exception;

    /**
     * Implement the initialization operations here. Throw a runtime exception
     * if you need to abort.
     */
    void initialize() throws Exception;

    /**
     * Implement the termination operations here. This method will be called if
     * initialize finished without exception.
     */
    void terminate() throws Exception;

    /**
     * Implement the main operations here.
     */
    void execute() throws Exception;
}

// END
