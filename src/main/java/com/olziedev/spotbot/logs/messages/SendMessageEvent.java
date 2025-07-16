package com.olziedev.spotbot.logs.messages;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SendMessageEvent extends SpotEvent {

    public SendMessageEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || this.isBlacklisted(event.getChannel())) return;

        manager.cachedMessages.put(event.getMessageIdLong(), event.getMessage());
    }
}
