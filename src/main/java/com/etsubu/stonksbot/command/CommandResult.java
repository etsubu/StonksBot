package com.etsubu.stonksbot.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * CommandResult contains the possible response of an executed command and whether it succeeded or not
 *
 * @author etsubu
 * @version 26 Jul 2018
 */
@Getter
@AllArgsConstructor
@Builder
public class CommandResult {
    private final boolean succeeded;
    private final boolean respondWithDM;
    private final String response;
    private final Exception exception;

    /**
     * Initializes CommandResult
     *
     * @param response  Text response of the executed command. Can be null
     * @param succeeded Whether the command succeeded or not
     */
    public CommandResult(String response, boolean succeeded) {
        this(succeeded, false, response, null);
    }

}
