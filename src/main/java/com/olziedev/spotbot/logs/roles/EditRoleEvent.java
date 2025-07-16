package com.olziedev.spotbot.logs.roles;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.update.*;

import java.awt.*;
import java.util.EnumSet;

public class EditRoleEvent extends SpotEvent {

    public EditRoleEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onRoleUpdateColor(RoleUpdateColorEvent event) {
        Role role = event.getRole();
        new ActionLog(null, "● Action: **Role Edited - Colour Change**\n● Role: **" + role.getAsMention() + "**\n● Role Colour Before: **#" + Integer.toHexString(event.getOldColorRaw()) + "**\n● Role Colour After: **#" + Integer.toHexString(event.getNewColorRaw()) + "**\n", Color.ORANGE);
    }

    @Override
    public void onRoleUpdateHoisted(RoleUpdateHoistedEvent event) {
        Role role = event.getRole();
        new ActionLog(null, "● Action: **Role Edited - Hoisted Change**\n● Role: **" + role.getAsMention() + "**\n● Role Hoisted Before: **" + event.getOldValue() + "**\n● Role Hoisted After: **" + event.getNewValue() + "**\n", Color.ORANGE);
    }

    @Override
    public void onRoleUpdateMentionable(RoleUpdateMentionableEvent event) {
        Role role = event.getRole();
        new ActionLog(null, "● Action: **Role Edited - Mentionable Change**\n● Role: **" + role.getAsMention() + "**\n● Role Mentionable Before: **" + event.getOldValue() + "**\n● Role Mentionable After: **" + event.getNewValue() + "**\n", Color.ORANGE);
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        Role role = event.getRole();
        new ActionLog(null, "● Action: **Role Edited - Name Change**\n● Role: **" + role.getAsMention() + "**\n● Role Name Before: **" + event.getOldName() + "**\n● Role Name After: **" + event.getNewName() + "**\n", Color.ORANGE);
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        Role role = event.getRole();
        StringBuilder set = new StringBuilder();
        StringBuilder removed = new StringBuilder();
        EnumSet<Permission> beforePermissions = event.getOldValue();
        EnumSet<Permission> afterPermissions = event.getNewValue();
        for (Permission afterPermission : afterPermissions) {
            if (!beforePermissions.contains(afterPermission)) set.append(afterPermission.getName()).append(", ");
        }

        for (Permission beforePermission : beforePermissions) {
            if (!afterPermissions.contains(beforePermission)) removed.append(beforePermission.getName()).append(", ");
        }
        new ActionLog(null, "● Action: **Role Edited - Permissions Change**\n● Role: **" + role.getAsMention() + "**\n● Role Permissions Set: **" + (set.toString().isEmpty() ? "N/A" : set.toString().trim().substring(0, set.toString().trim().length() - 1)) + "**\n● Role Permissions Removed: **" + (removed.toString().isEmpty() ? "N/A" : removed.toString().trim().substring(0, removed.toString().trim().length() - 1)) + "**\n", Color.RED);
    }
}
