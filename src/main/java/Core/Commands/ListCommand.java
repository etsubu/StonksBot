package Core.Commands;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListCommand extends Command implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    /**
     * Initializes Command
     */
    public ListCommand() {
        super("list");
    }

    @Override
    public CommandResult execute(String command) {
        if(applicationContext == null) {
            return new CommandResult("Not yet initialized, wait a moment and try again", true);
        }
        Map<String, Command> cmds = applicationContext.getBeansOfType(Command.class);
        StringBuilder builder = new StringBuilder();
        for(Command cmd : cmds.values()) {
            builder.append('!')
                    .append(cmd.getName())
                    .append('\n');
        }
        return new CommandResult(builder.toString(), true);
    }

    @Override
    public String help() {
        return "Lists all available commands\nUsage: !list";
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
