package com.olziedev.spotbot.logs.channels;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.channel.category.update.CategoryUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.store.update.StoreChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.*;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateBitrateEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateUserLimitEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;

public class EditChannelEvent extends SpotEvent {

    public EditChannelEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onStoreChannelUpdateName(@Nonnull StoreChannelUpdateNameEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **StoreChannel Name Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Name: **" + event.getOldName() + "**\n● New Name: **" + event.getNewName() + "**", Color.GREEN);
    }

    @Override
    public void onTextChannelUpdateName(@Nonnull TextChannelUpdateNameEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **TextChannel Name Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Name: **" + event.getOldName() + "**\n● New Name: **" + event.getNewName() + "**", Color.GREEN);
    }

    @Override
    public void onTextChannelUpdateTopic(@Nonnull TextChannelUpdateTopicEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **TextChannel Topic Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Topic: **" + (event.getOldTopic() == null ? "N/A" : event.getOldTopic()) + "**\n● New Topic: **" + (event.getNewTopic() == null ? "N/A" : event.getNewTopic()) + "**", Color.GREEN);
    }

    @Override
    public void onTextChannelUpdateNSFW(@Nonnull TextChannelUpdateNSFWEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **TextChannel NSFW Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old NSFW: **" + event.getOldNSFW() + "**\n● New NSFW: **" + event.getNewValue() + "**", Color.GREEN);
    }

    @Override
    public void onTextChannelUpdateParent(@Nonnull TextChannelUpdateParentEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **TextChannel Parent Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Parent: **" + (event.getOldParent() == null ? "N/A" : event.getOldParent().getAsMention()) + "**\n● New Parent: **" + (event.getNewParent() == null ? "N/A" : event.getNewParent().getAsMention()) + "**", Color.GREEN);
    }

    @Override
    public void onTextChannelUpdateSlowmode(@Nonnull TextChannelUpdateSlowmodeEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **TextChannel Slowmode Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Slowmode: **" + event.getOldSlowmode() + "**\n● New Slowmode: **" + event.getNewSlowmode() + "**", Color.GREEN);
    }

    @Override
    public void onVoiceChannelUpdateName(@Nonnull VoiceChannelUpdateNameEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **VoiceChannel Name Changed**\n● Old Name: **" + event.getOldName() + "**\n● New Name: **" + event.getNewName() + "**", Color.GREEN);
    }

    @Override
    public void onVoiceChannelUpdateUserLimit(@Nonnull VoiceChannelUpdateUserLimitEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **VoiceChannel User Limit Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Limit: **" + event.getOldUserLimit() + "**\n● New Limit: **" + event.getNewUserLimit() + "**", Color.GREEN);
    }

    @Override
    public void onVoiceChannelUpdateBitrate(@Nonnull VoiceChannelUpdateBitrateEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **VoiceChannel Bitrate Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Bitrate: **" + event.getOldBitrate() + "**\n● New Bitrate: **" + event.getNewBitrate() + "**", Color.GREEN);
    }

    @Override
    public void onVoiceChannelUpdateParent(@Nonnull VoiceChannelUpdateParentEvent event) {
        if (this.isBlacklisted(event.getChannel())) return;

        new ActionLog(null, "● Action: **VoiceChannel Parent Changed**\n● Channel: **" + event.getChannel().getName() + "**\n● Old Parent: **" + (event.getOldParent() == null ? "N/A" : event.getOldParent().getName()) + "**\n● New Parent: **" + (event.getNewParent() == null ? "N/A" : event.getNewParent().getName()) + "**", Color.GREEN);
    }

    @Override
    public void onCategoryUpdateName(@NotNull CategoryUpdateNameEvent event) {
        if (this.isBlacklisted(event.getCategory())) return;

        new ActionLog(null, "● Action: **Category Name Changed**\n● Old Name: **" + event.getOldName() + "**\n● New Name: **" + event.getNewName() + "**", Color.GREEN);
    }
}
