package com.olziedev.spotbot.logs.roles;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;

import javax.annotation.Nonnull;
import java.awt.*;

public class CreateRoleEvent extends SpotEvent {

    public CreateRoleEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        Role role = event.getRole();
        new ActionLog(null, "● Action: **Role Created**\n● Role: **" + role.getAsMention() + "**\n", Color.ORANGE);
    }
}
