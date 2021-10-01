package com.etsubu.stonksbot.administrative;

import com.etsubu.stonksbot.configuration.ConfigLoader;
import com.etsubu.stonksbot.configuration.ServerConfig;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
@AllArgsConstructor
public class GuildRoleManager extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(GuildRoleManager.class);
    private final ConfigLoader configLoader;

    private boolean handleRoleUpdate(List<String> requiredOrder, int position, Role role, Guild guild) {
        List<Role> roles = guild.getRoles();
        String roleId = role.getId();
        // Verify order
        int orderIndex = searchIndex(requiredOrder, roleId);
        if(orderIndex == -1 || orderIndex == requiredOrder.size() - 1) {
            // No order requirement
            return false;
        }
        Set<String> rolesToBeBelow = new HashSet<>(requiredOrder.subList(orderIndex + 1, requiredOrder.size()));
        int targetIndex = -1;
        for (int i  = 0; i < position; i++) {
            if(rolesToBeBelow.contains(roles.get(i).getId())) {
                targetIndex = i;
                break;
            }
        }
        if(targetIndex == -1) {
            // Order is ok
            return false;
        }
        // Change order
        log.info("Swapping role order {} -> {}", role.getName(), roles.get(targetIndex).getName());
        var cmd = guild.modifyRolePositions().selectPosition(role).swapPosition(roles.get(targetIndex));
        try {
            cmd.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to submit role swap", e);
        }
        return true;
    }

    private int searchIndex(List<String> indices, String id) {
        for(int i = 0; i < indices.size(); i++) {
            if(indices.get(i).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRoleUpdatePosition(RoleUpdatePositionEvent event) {
        Optional<List<String>> requiredRoleOrders = configLoader.getConfig().getServerConfig(event.getGuild().getId()).map(ServerConfig::getRequiredRoleOrders);
        if(requiredRoleOrders.isEmpty()) {
            log.info("No required role orders");
            // no role order configs for the guild
            return;
        }
        List<Role> roles = event.getGuild().getRoles();
        for(int i = 0; i < roles.size(); i++) {
            if(handleRoleUpdate(requiredRoleOrders.get(), i, roles.get(i), event.getGuild())) {
                return;
            }
        }
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {

    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {

    }

}
