package com.olziedev.spotbot.logs.members;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.awt.*;

public class JoinMemberEvent extends SpotEvent {

    public JoinMemberEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        new ActionLog(event.getMember(), "● Action: **Member Joined**\n● Member: **" + event.getMember().getUser().getAsTag() + "**", Color.GREEN);
    }
}
