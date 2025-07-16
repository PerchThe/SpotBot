package com.olziedev.spotbot.logs.messages;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EditMessageEvent extends SpotEvent {

    public EditMessageEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot() || event.getMember() == null) return;

        Message message = manager.cachedMessages.put(event.getMessageIdLong(), event.getMessage());
        if (message == null || event.getMessage().getContentRaw().isEmpty()) return;
        if (message.getAuthor().isBot() || this.isBlacklisted(message.getTextChannel())) return;
        String before = message.getContentRaw();
        if (before.length() > 1024) before = before.substring(0, 1021) + "...";
        String after = event.getMessage().getContentRaw();
        if (after.length() > 1024) after = after.substring(0, 1021) + "...";

        new ActionLog(event.getMember(), event.getMember(), "● Action: **Message Edited** - ([Jump To Message](" + message.getJumpUrl() + "))\n● Message Author: **" + message.getAuthor().getAsMention() + "**\n● Message Channel: **" + message.getTextChannel().getAsMention() + "**\n", Color.GREEN, new MessageEmbed.Field("**Message Content Before:**", before, false), new MessageEmbed.Field("**Message Content After:**", after, false));
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
            if (message.getAuthor().isBot() || this.isBlacklisted(message.getTextChannel())) return;

            new ActionLog(event.getMember(), event.getMember(), "● Action: **Reaction Added** - ([Jump To Message](" + message.getJumpUrl() + "))\n● Member: **" + event.getMember().getAsMention() + "**\n● Reaction: **" + (event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getEmote().getAsMention()) + "**", Color.ORANGE);
        }, error -> {});
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
            if (message.getAuthor().isBot() || this.isBlacklisted(message.getTextChannel())) return;
            new ActionLog(event.getMember(), event.getMember(), "● Action: **Reaction Removed** - ([Jump To Message](" + message.getJumpUrl() + "))\n● Member: **" + event.getMember().getAsMention() + "**\n● Reaction: **" + (event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getEmote().getAsMention()) + "**", Color.ORANGE);
        }, error -> {});
    }

    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
            if (message.getAuthor().isBot() || this.isBlacklisted(message.getTextChannel())) return;

            new ActionLog(null, "● Action: **Reaction Removed** - ([Jump To Message](" + message.getJumpUrl() + "))\n● Reaction: **" + (event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getEmote()) + "**", Color.ORANGE);
        }, error -> {});
    }
}
