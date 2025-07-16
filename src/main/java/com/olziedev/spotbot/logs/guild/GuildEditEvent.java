package com.olziedev.spotbot.logs.guild;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.update.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class GuildEditEvent extends SpotEvent {

    public GuildEditEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildUpdateAfkChannel(@NotNull GuildUpdateAfkChannelEvent event) {
        new ActionLog(null, "● Action: **AFK Channel Changed**\n● Old Channel: **" + (event.getOldAfkChannel() == null ? "N/A" : event.getOldAfkChannel().getAsMention()) + "**\n● New Channel: **" + (event.getNewAfkChannel() == null ? "N/A" : event.getNewAfkChannel().getAsMention()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateSystemChannel(@NotNull GuildUpdateSystemChannelEvent event) {
        new ActionLog(null, "● Action: **System Channel Changed**\n● Old Channel: **" + (event.getOldSystemChannel() == null ? "N/A" : event.getOldSystemChannel().getAsMention()) + "**\n● New Channel: **" + (event.getNewSystemChannel() == null ? "N/A" : event.getNewSystemChannel().getAsMention()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateRulesChannel(@NotNull GuildUpdateRulesChannelEvent event) {
        new ActionLog(null, "● Action: **Rules Channel Changed**\n● Old Channel: **" + (event.getOldRulesChannel() == null ? "N/A" : event.getOldRulesChannel().getAsMention()) + "**\n● New Channel: **" + (event.getNewRulesChannel() == null ? "N/A" : event.getNewRulesChannel().getAsMention()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateCommunityUpdatesChannel(@NotNull GuildUpdateCommunityUpdatesChannelEvent event) {
        new ActionLog(null, "● Action: **Community Updates Changed**\n● Old Channel: **" + (event.getOldCommunityUpdatesChannel() == null ? "N/A" : event.getOldCommunityUpdatesChannel().getAsMention()) + "**\n● New Channel: **" + (event.getNewCommunityUpdatesChannel() == null ? "N/A" : event.getNewCommunityUpdatesChannel().getAsMention()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateAfkTimeout(@NotNull GuildUpdateAfkTimeoutEvent event) {
        new ActionLog(null, "● Action: **AFK Timeout Changed**\n● Old Timeout: **" + event.getOldAfkTimeout().name() + "**\n● New Timeout: **" + event.getNewAfkTimeout().name() + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateExplicitContentLevel(@NotNull GuildUpdateExplicitContentLevelEvent event) {
        new ActionLog(null, "● Action: **Explicit Content Changed**\n● Old Level: **" + event.getOldLevel().name() + "**\n● New Level: **" + event.getNewLevel().name() + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateIcon(@NotNull GuildUpdateIconEvent event) {
        new ActionLog(null, "● Action: **Icon Changed**\n● Old Icon: **" + (event.getOldIconUrl() == null ? "N/A" : event.getOldIconUrl()) + "**\n● New Icon: **" + (event.getNewIconUrl() == null ? "N/A" : event.getNewIconUrl()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateName(@NotNull GuildUpdateNameEvent event) {
        new ActionLog(null, "● Action: **Name Changed**\n● Old Name: **" + event.getOldName() + "**\n● New Name: **" + event.getNewName() + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateNotificationLevel(@NotNull GuildUpdateNotificationLevelEvent event) {
        new ActionLog(null, "● Action: **Notification Level Changed**\n● Old Notification: **" + event.getOldNotificationLevel().name() + "**\n● New Notification: **" + event.getNewNotificationLevel().name() + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateOwner(@NotNull GuildUpdateOwnerEvent event) {
        new ActionLog(null, "● Action: **Owner Changed**\n● Old Owner: **" + event.getOldOwner().getAsMention() + "**\n● New Owner: **" + event.getNewOwner().getAsMention() + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateSplash(@NotNull GuildUpdateSplashEvent event) {
        new ActionLog(null, "● Action: **Splash Changed**\n● Old Splash: **" + (event.getOldSplashUrl() == null ? "N/A" : event.getOldSplashUrl()) + "**\n● New Splash: **" + (event.getNewSplashUrl() == null ? "N/A" : event.getNewSplashUrl()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateVerificationLevel(@NotNull GuildUpdateVerificationLevelEvent event) {
        new ActionLog(null, "● Action: **Verification Changed**\n● Old Verification: **" + event.getOldVerificationLevel().name() + "**\n● New Verification: **" + event.getNewValue().name() + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateVanityCode(@NotNull GuildUpdateVanityCodeEvent event) {
        new ActionLog(null, "● Action: **Vanity Code Changed**\n● Old Banner: **" + (event.getOldVanityUrl() == null ? "N/A" : event.getOldVanityUrl()) + "**\n● New Banner: **" + (event.getNewVanityUrl() == null ? "N/A" : event.getNewVanityUrl()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateBanner(@NotNull GuildUpdateBannerEvent event) {
        new ActionLog(null, "● Action: **Banner Changed**\n● Old Banner: **" + (event.getOldBannerUrl() == null ? "N/A" : event.getOldBannerUrl()) + "**\n● New Banner: **" + (event.getNewBannerUrl() == null ? "N/A" : event.getNewBannerUrl()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildUpdateDescription(@NotNull GuildUpdateDescriptionEvent event) {
        new ActionLog(null, "● Action: **Description Changed**\n● Old Description: **" + (event.getOldDescription() == null ? "N/A" : event.getOldDescription()) + "**\n● New Description: **" + (event.getNewDescription() == null ? "N/A" : event.getNewDescription()) + "**", Color.GREEN);
    }

    @Override
    public void onGuildInviteCreate(@NotNull GuildInviteCreateEvent event) {
        User user = event.getInvite().getInviter();
        new ActionLog(null, event.getInvite().getInviter(), "● Action: **Invite Created**\n● Member: **" + (user == null ? "N/A" : user.getAsMention()) + "**\n● Invite: **" + event.getUrl() + "**", Color.GREEN);
    }

    @Override
    public void onGuildInviteDelete(@NotNull GuildInviteDeleteEvent event) {
        new ActionLog(null, "● Action: **Invite Removed**\n● Invite: **" + event.getUrl() + "**", Color.ORANGE);
    }
}
