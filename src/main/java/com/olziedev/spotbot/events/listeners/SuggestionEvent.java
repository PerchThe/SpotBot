package com.olziedev.spotbot.events.listeners;

import com.olziedev.spotbot.commands.staff.ViewSuggestionsCommand;
import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.server.suggestion.Suggestion;
import com.olziedev.spotbot.server.suggestion.SuggestionStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

public class SuggestionEvent extends SpotEvent {

    public SuggestionEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Button button = event.getButton();
        if (button == null || button.getId() == null) return;
        System.out.println(button.getId());
        if (!button.getId().startsWith("suggestion")) return;

        ViewSuggestionsCommand.PageData pageData = ViewSuggestionsCommand.pages.get(event.getMessageIdLong());
        if (pageData != null) {
            switch (button.getId()) {
                case "suggestion_previous":
                    pageData.setCurrentPage(pageData.getCurrentPage() == 1 ? 1 : pageData.getCurrentPage() - 1);
                    break;
                case "suggestion_next":
                    pageData.setCurrentPage(pageData.getPages() == pageData.getCurrentPage() ? pageData.getPages() : pageData.getCurrentPage() + 1);
                    break;
            }
            ViewSuggestionsCommand.sendMessage(event, pageData, null);
            return;
        }
        Suggestion suggestion = manager.getSuggestion(event.getMessageIdLong());
        System.out.println("Found suggestion: " + suggestion);
        if (suggestion == null) return;

        switch (button.getId()) {
            case "suggestion_agree":
                suggestion.getMembersVoted().put(event.getUser().getIdLong(), SuggestionStatus.AGREE);
                break;
            case "suggestion_disagree":
                suggestion.getMembersVoted().put(event.getUser().getIdLong(), SuggestionStatus.DISAGREE);
                break;
        }
        suggestion.setMembersVoted(suggestion.getMembersVoted(), event);
    }
}
