package com.olziedev.spotbot.events.listeners;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.server.ticket.Ticket;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;

public class TicketDeleteEvent extends SpotEvent {

    public TicketDeleteEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        TextChannel channel = event.getChannel();
        Ticket ticket = manager.getTicket(channel);
        if (ticket == null) return;

        ticket.deleteTicket();
    }
}
