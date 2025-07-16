package com.olziedev.spotbot.logs.members;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class EditMemberEvent extends SpotEvent {

    public EditMemberEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        List<Role> roles = event.getRoles();
        new ActionLog(event.getMember(),"● Action: **Roles Added**\n● Member: **" + event.getMember().getAsMention() + "**\n● Roles Added: **" + roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", ")) + "**", Color.GREEN);
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        List<Role> roles = event.getRoles();
        new ActionLog(event.getMember(), "● Action: **Roles Removed**\n● Member: **" + event.getMember().getAsMention() + "**\n● Roles Removed: **" + roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", ")) + "**", Color.GREEN);
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        new ActionLog(event.getMember(), "● Action: **Nickname Changed**\n● Member: **" + event.getMember().getAsMention() + "**\n● Nickname Before: **" + (event.getOldNickname() == null ? event.getMember().getUser().getName() : event.getOldNickname()) + "**\n● Nickname After: **" + (event.getNewNickname() == null ? event.getMember().getUser().getName() : event.getNewNickname()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        new ActionLog(event.getMember(), "● Action: **Voice Join**\n● Member: **" + event.getMember().getAsMention() + "**\n● Channel: **" + event.getChannelJoined().getName() + "**", Color.GREEN);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        new ActionLog(event.getMember(), "● Action: **Voice Leave**\n● Member: **" + event.getMember().getAsMention() + "**\n● Channel: **" + event.getChannelLeft().getName() + "**", Color.GREEN);
    }
}
