package com.etsubu.stonksbot.command.utilities;

import com.etsubu.stonksbot.configuration.ServerConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Builder
@AllArgsConstructor
public class CommandContext {
    private final ServerConfig serverConfig;
    private final User sender;
    private final List<Role> userRoles;
    private final String message;
    private final Guild guild;

    public String getMessage() { return message; }

    public User getSender() { return sender; }

    public Optional<ServerConfig> getServerConfig() { return Optional.ofNullable(serverConfig); }

    public List<Role> getUserRoles() { return Optional.ofNullable(userRoles).orElseGet(LinkedList::new); }

    public Optional<Guild> getGuild() { return Optional.ofNullable(guild); }
}
