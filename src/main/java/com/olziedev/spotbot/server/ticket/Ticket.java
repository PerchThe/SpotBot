package com.olziedev.spotbot.server.ticket;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.entities.*;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Ticket {

    private final long id;
    private long userID = -1;
    private String category;
    private Message embedMessage;

    private final DatabaseManager manager;

    public Ticket(long id) {
        this.manager = SpotBot.getDatabaseManager();
        this.id = id;

        TextChannel textChannel = this.getTextChannel();
        if (textChannel == null) {
            this.deleteTicket();
            return;
        }

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM spot_tickets WHERE id = ?");
            ps.setLong(1, this.id);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                this.userID = result.getLong("owner_id");
                this.category = result.getString("category");
                textChannel.retrieveMessageById(result.getLong("embed_message_id")).queue(m -> this.embedMessage = m, f -> {
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isOwner(Member member) {
        long user = this.userID;
        return member != null && member.getIdLong() == user;
    }

    public long getID() {
        return this.id;
    }

    public TextChannel getTextChannel() {
        return manager.getJDA().getTextChannelById(this.id);
    }

    public long getUserID() {
        return this.userID;
    }

    public TicketCategory getCategory() {
        return SpotBot.getServer().getTicketCreator().getCategories().stream().filter(x -> x.getKey().equals(this.category)).findFirst().orElse(null);
    }

    public void deleteTicket() {
        manager.removeTicket(this);

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM spot_tickets WHERE id = ?");
            ps.setLong(1, this.id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close(Member closed, long date) {
        TextChannel channel = this.getTextChannel();
        new TicketTranscript(this).createTranscript(url -> {
            if (url == null) return;

            if (Configuration.getConfig().getBoolean("settings.ticket.send-transcript-to-added-users")) {
                List<Member> members = new ArrayList<>(channel.getMemberPermissionOverrides()).stream().filter(PermissionOverride::isMemberOverride).map(PermissionOverride::getMember).collect(Collectors.toList());
                for (Member member : members) {
                    if (member.getIdLong() == manager.jda.getSelfUser().getIdLong()) continue;

                    if (member.getIdLong() == this.userID) {
                            String creator;
                            try {
                                creator = this.manager.jda.getUserById(this.userID).getAsTag();
                            } catch (final Exception ex) {
                                creator = "Null (Member Left)?";
                            }
                            try {
                                PrivateChannel dm = member.getUser().openPrivateChannel().complete();
                                SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.transcript-user-sent-owner"), null);
                                spotMessage.replaceOptions("%closed%", creator);
                                spotMessage.replaceOptions("%creator%", manager.jda.getUserById(this.userID).getAsTag());
                                spotMessage.replaceOptions("%category%", this.getCategory().getLabel());
                                spotMessage.replaceOptions("%date%", "<t:" + date + ">");
                                spotMessage.replaceOptions("%url%", url);
                                if (spotMessage.hasEmbed()) {
                                    spotMessage.getSpotEmbed().replaceOptions("%closed%", creator);
                                    spotMessage.getSpotEmbed().replaceOptions("%creator%", manager.jda.getUserById(this.userID).getAsTag());
                                    spotMessage.getSpotEmbed().replaceOptions("%category%", this.getCategory().getLabel());
                                    spotMessage.getSpotEmbed().replaceOptions("%date%", "<t:" + date + ">");
                                    spotMessage.replaceOptions("%url%", url);
                                }
                                spotMessage.sendMessage(dm).complete();
                            } catch (Throwable ignored) {
                            }
                            continue;
                    }
                    String creator;
                    try {
                        creator = this.manager.jda.getUserById(this.userID).getAsTag();
                    } catch (final Exception ex) {
                        creator = "Null (Member Left)?";
                    }
                    try {
                        PrivateChannel dm = member.getUser().openPrivateChannel().complete();
                        SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.transcript-user-sent"), null);
                        spotMessage.replaceOptions("%closed%", creator);
                        spotMessage.replaceOptions("%creator%", manager.jda.getUserById(this.userID).getAsTag());
                        spotMessage.replaceOptions("%category%", this.getCategory().getLabel());
                        spotMessage.replaceOptions("%date%", "<t:" + date + ">");
                        spotMessage.replaceOptions("%url%", url);
                        if (spotMessage.hasEmbed()) {
                            spotMessage.getSpotEmbed().replaceOptions("%closed%", creator);
                            spotMessage.getSpotEmbed().replaceOptions("%creator%", manager.jda.getUserById(this.userID).getAsTag());
                            spotMessage.getSpotEmbed().replaceOptions("%category%", this.getCategory().getLabel());
                            spotMessage.getSpotEmbed().replaceOptions("%date%", "<t:" + date + ">");
                            spotMessage.replaceOptions("%url%", url);
                        }
                        spotMessage.sendMessage(dm).complete();
                    } catch (Throwable ignored) {}
                }
            }
            String creator;
            try {
                creator = this.manager.jda.getUserById(this.userID).getAsTag();
            } catch (final Exception ex) {
                creator = "Null (Member Left)?";
            }
            try {
                TextChannel textChannel = manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.ticket.transcript-channel-id"));
                SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.transcript-sent"), null);
                spotMessage.replaceOptions("%closed%", creator);
                spotMessage.replaceOptions("%creator%", manager.jda.getUserById(this.userID).getAsTag());
                spotMessage.replaceOptions("%category%", this.getCategory().getLabel());
                spotMessage.replaceOptions("%date%", "<t:" + date + ">");
                spotMessage.replaceOptions("%url%", url);
                if (spotMessage.hasEmbed()) {
                    spotMessage.getSpotEmbed().replaceOptions("%closed%", creator);
                    spotMessage.getSpotEmbed().replaceOptions("%creator%", manager.jda.getUserById(this.userID).getAsTag());
                    spotMessage.getSpotEmbed().replaceOptions("%category%", this.getCategory().getLabel());
                    spotMessage.getSpotEmbed().replaceOptions("%date%", "<t:" + date + ">");
                    spotMessage.replaceOptions("%url%", url);
                }
                spotMessage.sendMessage(textChannel).complete();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
            channel.delete().queue(success -> {}, error -> {});
        });
    }
}
