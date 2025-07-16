package com.olziedev.spotbot.logs.messages;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteMessageEvent extends SpotEvent {

    public DeleteMessageEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        Message message = manager.cachedMessages.remove(event.getMessageIdLong());
        if (message == null) return;
        if (message.getAuthor().isBot() || this.isBlacklisted(message.getTextChannel())) return;

        String before = message.getContentRaw();
        if (before.length() > 1024) before = before.substring(0, 1021) + "...";

        List<Member> members = message.getMentionedMembers();
        new ActionLog(message.getMember(), "● Action: **Message Deleted**\n● Message Author: **" + message.getAuthor().getAsMention() + "**\n● Message Channel: **" + message.getTextChannel().getAsMention() + "**" + (!members.isEmpty() ? "\n● Mentioned Members: " + members.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", ")) : ""), message.getAttachments(), Color.ORANGE, new MessageEmbed.Field("**Message Content:**", before, false));
    }
}
