package com.olziedev.spotbot.events.listeners;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.server.suggestion.Suggestion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import org.jetbrains.annotations.NotNull;

public class SuggestionDeleteEvent extends SpotEvent {

    public SuggestionDeleteEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        Suggestion suggestion = manager.getSuggestion(event.getMessageIdLong());
        if (suggestion == null) return;

        suggestion.delete();
    }
}
