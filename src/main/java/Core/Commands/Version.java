package Core.Commands;

import org.springframework.stereotype.Component;

/**
 * Version command returns the current bot version
 */
@Component
public class Version extends Command {
    public static final String VERSION = "1.3.5";
    /**
     * Initializes version
     */
    public Version() {
        super("version");
    }

    public String getVersion() { return VERSION; }

    @Override
    public CommandResult execute(String command) {
        return new CommandResult("Version: " + VERSION + "\nSource: https://github.com/etsubu/KeisariBot", true);
    }

    @Override
    public String help() {
        return "Usage !version";
    }
}
