package Core.Commands;

import Core.Utilities.ArchiveTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AboutCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(AboutCommand.class);
    private static final String name = "about";
    private final CommandResult result;

    /**
     * Initializes the author command
     */
    public AboutCommand() {
        super(name);
        result = new CommandResult("StonksBot - " + ArchiveTools.getApplicationVersion().orElse("Unknown version") +
                "\nAuthor Jarre Leskinen"
                + "\nSource code and documentation: https://github.com/etsubu/StonksBot", true);
        log.info("Initialized {}", AboutCommand.class.getName());
    }

    @Override
    public CommandResult execute(String command) {
        return result;
    }

    @Override
    public String help() {
        return "Displays the application name, version number, author and link to source code"
                + "\n\tUsage: !" + name;
    }
}
