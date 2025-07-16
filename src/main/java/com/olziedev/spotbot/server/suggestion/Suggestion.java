package com.olziedev.spotbot.server.suggestion;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Suggestion {

    private final long id;
    private long messageID;
    private long userID;
    private String suggestion;
    private Map<Long, SuggestionStatus> membersVoted;

    private final DatabaseManager manager;

    public Suggestion(long id) {
        this.manager = SpotBot.getDatabaseManager();
        this.id = id;
        this.membersVoted = new ConcurrentHashMap<>();

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM spot_suggestions WHERE id = ?");
            ps.setLong(1, this.id);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                this.messageID = result.getLong("message_id");
                this.userID = result.getLong("user_id");
                this.suggestion = result.getString("suggestion");
                String membersVoted = result.getString("members_voted");
                if (membersVoted != null && !membersVoted.isEmpty()) {
                    for (String s : membersVoted.split(",")) {
                        String[] split = s.split(":");
                        this.membersVoted.put(Long.parseLong(split[0]), SuggestionStatus.valueOf(split[1]));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long getID() {
        return this.id;
    }

    public long getMessageID() {
        return this.messageID;
    }

    public long getUserID() {
        return this.userID;
    }

    public String getSuggestion() {
        return this.suggestion;
    }

    public void approve(String reason, Member member, Consumer<Long> callback) {
        TextChannel channel = manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.channel-id"));
        if (channel == null) return;

        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.suggestion.suggestion-message-accepted"), null);
        StringBuilder results = new StringBuilder();
        channel.retrieveMessageById(this.messageID).queue(msg -> {
            Arrays.stream(SuggestionStatus.values()).forEach(x -> {
                results.append("**").append(Configuration.getConfig().getString("settings.suggestion." + x.name().toLowerCase() + "-reaction")).append(": ").append(String.valueOf(x.getMembers(this.membersVoted).size())).append("**\n");
            });
            if (spotMessage.hasEmbed()) {
                User user = manager.getJDA().getUserById(this.userID);
                spotMessage.getSpotEmbed().replaceOptions("%response%", reason);
                spotMessage.getSpotEmbed().replaceOptions("%suggestion%", suggestion);
                spotMessage.getSpotEmbed().replaceOptions("%suggestion_id%", String.valueOf(id));
                spotMessage.getSpotEmbed().replaceOptions("%member%", user == null ? String.valueOf(this.userID) : user.getAsMention());
                spotMessage.getSpotEmbed().replaceOptions("%member_id%", String.valueOf(this.userID));
                spotMessage.getSpotEmbed().replaceOptions("%results%", results.toString());
                spotMessage.getSpotEmbed().replaceOptions("%accepted_member%", member.getAsMention());
                spotMessage.getSpotEmbed().replaceOptions("%agree_amount%", String.valueOf(SuggestionStatus.AGREE.getMembers(this.membersVoted).size()));
                spotMessage.getSpotEmbed().replaceOptions("%disagree_amount%", String.valueOf(SuggestionStatus.DISAGREE.getMembers(this.membersVoted).size()));
                spotMessage.getSpotEmbed().getEmbedBuilder().setThumbnail(user == null ? member.getEffectiveAvatarUrl() : user.getEffectiveAvatarUrl());
            }
            spotMessage.sendMessage(manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.accepted-channel-id"))).setActionRows().queue(x -> callback.accept(x.getIdLong()));
            msg.delete().queue();
            this.delete();
        });
    }

    public void deny(String reason, Member member, Consumer<Long> callback) {
        TextChannel channel = manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.channel-id"));
        if (channel == null) return;

        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.suggestion.suggestion-message-denied"), null);
        StringBuilder results = new StringBuilder();
        channel.retrieveMessageById(this.messageID).queue(msg -> {
            Arrays.stream(SuggestionStatus.values()).forEach(x -> {
                results.append("**").append(Configuration.getConfig().getString("settings.suggestion." + x.name().toLowerCase() + "-reaction")).append(": ").append(String.valueOf(x.getMembers(this.membersVoted).size())).append("**\n");
            });
            if (spotMessage.hasEmbed()) {
                User user = manager.getJDA().getUserById(this.userID);
                spotMessage.getSpotEmbed().replaceOptions("%response%", reason);
                spotMessage.getSpotEmbed().replaceOptions("%suggestion%", suggestion);
                spotMessage.getSpotEmbed().replaceOptions("%suggestion_id%", String.valueOf(id));
                spotMessage.getSpotEmbed().replaceOptions("%member%", user == null ? String.valueOf(this.userID) : user.getAsMention());
                spotMessage.getSpotEmbed().replaceOptions("%member_id%", String.valueOf(this.userID));
                spotMessage.getSpotEmbed().replaceOptions("%results%", results.toString());
                spotMessage.getSpotEmbed().replaceOptions("%rejected_member%", member.getAsMention());
                spotMessage.getSpotEmbed().replaceOptions("%agree_amount%", String.valueOf(SuggestionStatus.AGREE.getMembers(this.membersVoted).size()));
                spotMessage.getSpotEmbed().replaceOptions("%disagree_amount%", String.valueOf(SuggestionStatus.DISAGREE.getMembers(this.membersVoted).size()));
                spotMessage.getSpotEmbed().getEmbedBuilder().setThumbnail(user == null ? member.getEffectiveAvatarUrl() : user.getEffectiveAvatarUrl());
            }
            spotMessage.sendMessage(manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.denied-channel-id"))).setActionRows().queue(x -> callback.accept(x.getIdLong()));
            msg.delete().queue();
            this.delete();
        });
    }

    public void consider() {
        TextChannel channel = manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.channel-id"));
        if (channel == null) return;

        channel.retrieveMessageById(this.messageID).queue(m -> {
            SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.suggestion.suggestion-message-considered"), null, embed -> {
                MessageEmbed messageEmbed = m.getEmbeds().get(0);
                MessageEmbed.Field field = messageEmbed.getFields().stream().filter(x -> x.getName().contains("Note by")).findFirst().orElse(null);
                if (field == null) return;

                embed.addField(field);
            });
            if (spotMessage.hasEmbed()) {
                User user = manager.getJDA().getUserById(this.userID);
                spotMessage.getSpotEmbed().replaceOptions("%suggestion%", suggestion);
                spotMessage.getSpotEmbed().replaceOptions("%suggestion_id%", String.valueOf(id));
                spotMessage.getSpotEmbed().replaceOptions("%member%", user == null ? String.valueOf(this.userID) : user.getAsMention());
                spotMessage.getSpotEmbed().replaceOptions("%member_id%", String.valueOf(this.userID));
                spotMessage.getSpotEmbed().replaceOptions("%agree_amount%", String.valueOf(SuggestionStatus.AGREE.getMembers(this.membersVoted).size()));
                spotMessage.getSpotEmbed().replaceOptions("%disagree_amount%", String.valueOf(SuggestionStatus.DISAGREE.getMembers(this.membersVoted).size()));
                spotMessage.getSpotEmbed().getEmbedBuilder().setThumbnail(user == null ? null : user.getEffectiveAvatarUrl());
            }
            spotMessage.editMessage(m).setActionRows().queue();
        });
    }

    public void setNote(String note, Member member) {
        TextChannel channel = manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.channel-id"));
        if (channel == null) return;

        channel.retrieveMessageById(this.messageID).queue(m -> {
            MessageEmbed messageEmbed = m.getEmbeds().get(0);
            EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);

            embedBuilder.clearFields();
            embedBuilder.addField("Note by " + member.getUser().getAsTag(), note + "\n\u200E", false);
            messageEmbed.getFields().stream().filter(x -> !x.getName().contains("Note by")).forEach(embedBuilder::addField);
            m.editMessageEmbeds(embedBuilder.build()).queue();
        });
    }

    public void delete() {
        manager.removeSuggestion(this);

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM spot_suggestions WHERE id = ?");
            ps.setLong(1, this.id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Map<Long, SuggestionStatus> getMembersVoted() {
        return this.membersVoted;
    }

    public void setMembersVoted(Map<Long, SuggestionStatus> membersVoted, ComponentInteraction event) {
        this.membersVoted = membersVoted == null ? new ConcurrentHashMap<>() : membersVoted;

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE spot_suggestions SET members_voted = ? WHERE id = ?");
            ps.setString(1, this.membersVoted.isEmpty() ? null : this.membersVoted.entrySet().stream().map(x -> x.getKey() + ":" + x.getValue()).collect(Collectors.joining(",")));
            ps.setLong(2, this.id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.suggestion.suggestion-message"), null);
        User user = manager.getJDA().getUserById(this.userID);
        if (spotMessage.hasEmbed()) {
            spotMessage.getSpotEmbed().replaceOptions("%suggestion%", suggestion);
            spotMessage.getSpotEmbed().replaceOptions("%suggestion_id%", String.valueOf(id));
            spotMessage.getSpotEmbed().replaceOptions("%member%", user.getAsTag());
            spotMessage.getSpotEmbed().replaceOptions("%member_id%", user.getId());
            spotMessage.getSpotEmbed().replaceOptions("%member_avatar%", user.getEffectiveAvatarUrl());
            spotMessage.getSpotEmbed().replaceOptions("%agree_amount%", String.valueOf(SuggestionStatus.AGREE.getMembers(this.membersVoted).size()));
            spotMessage.getSpotEmbed().replaceOptions("%disagree_amount%", String.valueOf(SuggestionStatus.DISAGREE.getMembers(this.membersVoted).size()));
            spotMessage.getSpotEmbed().getEmbedBuilder().setThumbnail(user.getEffectiveAvatarUrl());
        }
        spotMessage.editMessage(event).queue();
    }

    public static void create(String suggestionText, Member member, Consumer<Suggestion> callback) {
        DatabaseManager manager = SpotBot.getDatabaseManager();
        TextChannel channel = manager.getJDA().getTextChannelById(Configuration.getConfig().getLong("settings.suggestion.channel-id"));

        Suggestion suggestion = null;
        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO spot_suggestions(suggestion, user_id) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, suggestionText);
            ps.setLong(2, member.getIdLong());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                suggestion = new Suggestion(rs.getLong(1));
                manager.addSuggestion(suggestion);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (suggestion == null) {
            callback.accept(null);
            return;
        }

        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.suggestion.suggestion-message"), null);
        if (spotMessage.hasEmbed()) {
            spotMessage.getSpotEmbed().replaceOptions("%suggestion%", suggestion.suggestion);
            spotMessage.getSpotEmbed().replaceOptions("%suggestion_id%", String.valueOf(suggestion.id));
            spotMessage.getSpotEmbed().replaceOptions("%member%", member.getUser().getAsTag());
            spotMessage.getSpotEmbed().replaceOptions("%member_id%", member.getId());
            spotMessage.getSpotEmbed().replaceOptions("%member_avatar%", member.getEffectiveAvatarUrl());
            spotMessage.getSpotEmbed().replaceOptions("%agree_amount%", "0");
            spotMessage.getSpotEmbed().replaceOptions("%disagree_amount%", "0");
            spotMessage.getSpotEmbed().getEmbedBuilder().setThumbnail(member.getEffectiveAvatarUrl());
        }
        Suggestion finalSuggestion = suggestion;
        spotMessage.sendMessage(channel).queue(m -> {
            try {
                Connection con = manager.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE spot_suggestions SET message_id = ? WHERE id = ?");
                ps.setLong(1, m.getIdLong());
                ps.setLong(2, finalSuggestion.id);
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            finalSuggestion.messageID = m.getIdLong();
            callback.accept(finalSuggestion);
            m.createThreadChannel("Thread for suggestion " + finalSuggestion.id).queue();

        });
    }
}
