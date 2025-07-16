package com.olziedev.spotbot.commands.staff;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.suggestion.Suggestion;
import com.olziedev.spotbot.server.suggestion.SuggestionStatus;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ViewSuggestionsCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("viewsuggestions-command");
    public static final Map<Long, PageData> pages = new ConcurrentHashMap<>();
    private static final int MEMBERS_PER_PAGE = 10;

    public ViewSuggestionsCommand() {
        super(commands.getString("command"));
        this.setDescription(commands.getString("description"));
        this.setRoles(commands.getStringList("roles").stream().map(Long::parseLong).toArray(Long[]::new));

        for (String option : commands.getSection("options").singleLayerKeySet()) {
            FlatFileSection options = commands.getSection("options." + option);
            this.addOption(new OptionData(options.getEnum("type", OptionType.class), options.getString("name"), options.getString("description"), options.getBoolean("required")) {{
                FlatFileSection choices = options.getSection("choices");
                if (choices != null) {
                    for (String choice : choices.singleLayerKeySet()) {
                        this.addChoice(choice, choices.getString(choice));
                    }
                }
            }});
        }
    }

    @Override
    public void onExecute(SlashExecutor cmd) {
        long suggestionID = cmd.getOption("suggest-id").getAsLong();
        Suggestion suggestion = SpotBot.getDatabaseManager().getSuggestions().get(suggestionID);
        if (suggestion == null) {
            new SpotMessage(Configuration.getLang().getSection("lang.suggestion-id-not-found"), cmd).sendMessage().queue();
            return;
        }
        SuggestionStatus status = SuggestionStatus.valueOf(cmd.getOption("status").getAsString().toUpperCase());
        sendMessage(null, new PageData(1, suggestion, status), cmd.getEvent());
    }

    public static void sendMessage(ComponentInteraction interaction, PageData pageData, CommandInteraction channel) {
        List<Long> members = pageData.status.getMembers(pageData.suggestion.getMembersVoted());
        if (members.isEmpty()) {
            new SpotMessage(Configuration.getLang().getSection("lang.no-members-found"), null).sendMessage(channel).queue();
            return;
        }
        int pages = (int) Math.ceil((double) members.size() / MEMBERS_PER_PAGE);
        pageData.setPages(pages);
        int start = (pageData.currentPage - 1) * MEMBERS_PER_PAGE;
        List<Long> membersOnPage = members.subList((pageData.currentPage - 1) * MEMBERS_PER_PAGE, Math.min(start + MEMBERS_PER_PAGE, members.size()));
        StringBuilder builder = new StringBuilder();
        for (Long member : membersOnPage) {
            builder.append("<@").append(member).append(">\n");
        }
        Suggestion suggestion = pageData.suggestion;
        SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.suggestion-members"), null);
        spotMessage.replaceOptions("%members%", builder.toString());
        spotMessage.replaceOptions("%page%", String.valueOf(pageData.currentPage));
        spotMessage.replaceOptions("%pages%", String.valueOf(pages));
        spotMessage.replaceOptions("%suggestion-id%", String.valueOf(suggestion.getID()));
        spotMessage.replaceOptions("%status%", pageData.status.name().toLowerCase());
        spotMessage.getSpotEmbed().replaceOptions("%members%", builder.toString());
        spotMessage.getSpotEmbed().replaceOptions("%page%", String.valueOf(pageData.currentPage));
        spotMessage.getSpotEmbed().replaceOptions("%pages%", String.valueOf(pages));
        spotMessage.getSpotEmbed().replaceOptions("%suggestion-id%", String.valueOf(suggestion.getID()));
        spotMessage.getSpotEmbed().replaceOptions("%status%", pageData.status.name().toLowerCase());

        Supplier<ActionRow> rows = () -> {
            Button previous = spotMessage.getButton("suggestion_previous");
            Button next = spotMessage.getButton("suggestion_next");
            if (previous == null || next == null) return null;

            // then alter the buttons if needed. if the page is 1, then we disable the back button
            // if the page is the last page, then we disable the next button
            if (pageData.currentPage == 1) previous = previous.asDisabled();
            if (pageData.currentPage == pages) next = next.asDisabled();

            return ActionRow.of(previous, next);
        };
        Consumer<InteractionHook> msg = x -> {
            ViewSuggestionsCommand.pages.put(x.getInteraction().getIdLong(), pageData);
        };
        ActionRow rowsList = rows.get();
        spotMessage.getButtons().clear();
        if (channel != null) spotMessage.sendMessage(channel).addActionRows(rowsList).queue(msg);
        if (interaction != null) spotMessage.editMessage(interaction).setActionRows(rowsList).queue(msg);
    }

    public static class PageData {

        private int currentPage;
        private int pages;
        private final Suggestion suggestion;
        private final SuggestionStatus status;

        public PageData(int currentPage, Suggestion suggestion, SuggestionStatus status) {
            this.currentPage = currentPage;
            this.suggestion = suggestion;
            this.status = status;
        }

        private void setPages(int pages) {
            this.pages = pages;
        }

        public int getCurrentPage() {
            return this.currentPage;
        }

        public int getPages() {
            return this.pages;
        }

        public Suggestion getSuggestion() {
            return this.suggestion;
        }

        public SuggestionStatus getStatus() {
            return this.status;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
    }
}
