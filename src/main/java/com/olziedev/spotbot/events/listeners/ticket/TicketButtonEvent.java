package com.olziedev.spotbot.events.listeners.ticket;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.ticket.Ticket;
import com.olziedev.spotbot.server.ticket.TicketCategory;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TicketButtonEvent extends SpotEvent {

    public TicketButtonEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Ticket ticket = manager.getTicket(event.getTextChannel());
        Button button = event.getButton();
        if (button == null || button.getId() == null || !button.getId().startsWith("ticket_") || ticket == null)
            return;

        TicketCategory category = ticket.getCategory();
        if (category == null) return;

        switch (button.getId().replace("ticket_", "")) {
            case "confirm_yes":
                this.disableButtons(event, event.getMessage().getEmbeds().get(0), null);
                ticket.close(event.getMember(), event.getMessage().getTimeCreated().toInstant().getEpochSecond());
                return;
            case "confirm_no":
                this.disableButtons(event, event.getMessage().getEmbeds().get(0), null);
                SpotMessage confirmMessage = new SpotMessage(Configuration.getConfig().getSection("settings.ticket.categories." + category.getKey() + ".ticket-confirmation-cancel-message"), null);
                if (confirmMessage.hasEmbed()) {
                    confirmMessage.getSpotEmbed().replaceOptions("%member%", event.getMember().getAsMention());
                }
                confirmMessage.sendMessage(event).queue();
        }
    }

    private void disableButtons(ButtonClickEvent event, MessageEmbed eb, Predicate<Button> button) {
        event.editMessageEmbeds(eb).queue(m -> m.editOriginalEmbeds(eb).setActionRow(event.getMessage().getButtons().stream().map(x ->
                button == null ? x.asDisabled() : button.test(x) ? x.asDisabled() : x).collect(Collectors.toList())).queue());
    }
}
