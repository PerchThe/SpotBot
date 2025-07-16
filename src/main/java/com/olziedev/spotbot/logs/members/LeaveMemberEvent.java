package com.olziedev.spotbot.logs.members;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

import java.awt.*;

public class LeaveMemberEvent extends SpotEvent {

    public LeaveMemberEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        new ActionLog(event.getMember(), "● Action: **Member Left**\n● Member: **" + event.getMember().getUser().getAsTag() + "**", Color.ORANGE);
    }
}
