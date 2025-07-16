package com.olziedev.spotbot.logs.roles;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;

import java.awt.*;

public class DeleteRoleEvent extends SpotEvent {

    public DeleteRoleEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        Role role = event.getRole();
        new ActionLog(null, "● Action: **Role Deleted**\n● Role: **" + role.getName() + "**\n● Role Colour: **" + (role.getColor() != null ? "#" + Integer.toHexString(role.getColorRaw()) : "N/A") + "**\n", Color.RED);
    }
}
