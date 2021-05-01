package com.etsubu.stonksbot.command;

/**
 * CommandResult contains the possible response of an executed command and whether it succeeded or not
 * @author etsubu
 * @version 26 Jul 2018
 *
 */
public class CommandResult {
    private final boolean succeeded;
    private final String response;
    private final Exception exception;
    
    /**
     * Initializes CommandResult
     * @param response Text response of the executed command. Can be null
     * @param succeeded Whether the command succeeded or not
     */
    public CommandResult(String response, boolean succeeded) {
        this(response, succeeded, null);
    }

    /**
     * Initializes CommandResult
     * @param response Text response of the executed command. Can be null
     * @param succeeded Whether the command succeeded or not
     * @param exception Exception that caused the command failed. Can be null
     */
    public CommandResult(String response, boolean succeeded, Exception exception) {
        this.response = response;
        this.succeeded = succeeded;
        this.exception = exception;
    }

    /**
     * 
     * @return Text response of the executed command
     */
    public String getResponse() {
        return this.response;
    }
    
    /**
     * 
     * @return Whether the command succeeded or not
     */
    public boolean getSucceeded() {
        return this.succeeded;
    }

    /**
     *
     * @return Exception that caused the command to fail
     */
    public Exception getException() {
        return this.exception;
    }

}
