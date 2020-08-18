package Core.Commands;

/**
 * Command class defines behaviour for a single command
 * @author etsubu
 * @version 26 Jul 2018
 *
 */
public abstract class Command {
    /**
     * Contains the name of the command that follows prefix
     */
    protected String name;
    
    /**
     * Initializes Command
     * @param name Command name that follows the prefix
     */
    public Command(String name) {
        this.name = name;
    }
    
    /**
     * 
     * @return Name of the command
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Defines command execution
     * @param command Command the user typed
     * @return CommandResult containing the result
     */
    public abstract CommandResult execute(String command);

    /**
     * @return Help texts telling the user how to use the command
     */
    public abstract String help();
}