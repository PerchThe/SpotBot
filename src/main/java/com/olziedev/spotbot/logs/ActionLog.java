package com.olziedev.spotbot.logs;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ActionLog {

    public ActionLog(Member member, String content, Color level, MessageEmbed.Field... extraFields) {
        this(member, content, null, level, extraFields);
    }

    public ActionLog(Member member, String content, java.util.List<Message.Attachment> attachments, Color level, MessageEmbed.Field... extraFields) {
        this(member, (Member) null, content, attachments, level, extraFields);
    }

    public ActionLog(Member member, @Nullable Member executor, String content, Color level, MessageEmbed.Field... extraFields) {
        this(member, executor == null ? null : executor.getUser(), content, null, level, extraFields);
    }

    public ActionLog(Member member, @Nullable Member executor, String content, java.util.List<Message.Attachment> attachments, Color level, MessageEmbed.Field... extraFields) {
        this(member, executor == null ? null : executor.getUser(), content, attachments, level, extraFields);
    }

    public ActionLog(Member member, User executor, String content, Color level, MessageEmbed.Field... extraFields) {
        this(member, executor, content, null, level, extraFields);
    }

    public ActionLog(Member member, User executor, String content, List<Message.Attachment> attachments, Color level, MessageEmbed.Field... extraFields) {
        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.logging.message"), null);
        if (!spotMessage.hasEmbed()) {
            SpotBot.getLogger().info("Action logger doesn't have an embed!");
            return;
        }
        spotMessage.replaceOptions("%action%", content);
        spotMessage.replaceOptions("%user%", (executor == null ? "N/A" : executor.getAsTag()));

        spotMessage.getSpotEmbed().replaceOptions("%action%", content);
        spotMessage.getSpotEmbed().replaceOptions("%user%", (executor == null ? "N/A" : executor.getAsTag()));
        MessageEmbed oldEmbed = spotMessage.getSpotEmbed().build();
        EmbedBuilder eb = spotMessage.getSpotEmbed().getEmbedBuilder();
        eb.setColor(level).setThumbnail(member == null ? SpotBot.getServer().getGuild().getIconUrl() : member.getEffectiveAvatarUrl());
        if (oldEmbed.getFooter() != null) eb.setFooter(oldEmbed.getFooter().getText(), executor == null ? null : executor.getEffectiveAvatarUrl());

        if (extraFields != null) for (MessageEmbed.Field fields : extraFields) eb.addField(fields);

        MessageAction action = SpotBot.getServer().getLogChannel().sendMessageEmbeds(eb.build());
        for (Message.Attachment attachment : attachments == null ? Collections.<Message.Attachment>emptyList() : attachments) {
            try {
                action = action.addFile(attachment.retrieveInputStream().get(), attachment.getFileName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        action.queue();
    }
}
