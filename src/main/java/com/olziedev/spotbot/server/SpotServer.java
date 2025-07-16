package com.olziedev.spotbot.server;

import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.reaction.SpotReaction;
import com.olziedev.spotbot.server.ticket.TicketCreator;
import com.olziedev.spotbot.utils.Configuration;
import com.olziedev.spotbot.utils.SpotTimer;
import com.olziedev.spotbot.utils.Utils;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpotServer {

    private final JDA jda;
    private final List<Activity> activities;
    private TicketCreator ticketCreator;

    public SpotServer(JDA jda) {
        this.jda = jda;
        this.activities = new ArrayList<>();
    }

    public void statusUpdater() {
        // load all the activities from the config file.
        FlatFileSection presence = Configuration.getConfig().getSection("settings.presence");
        this.jda.getPresence().setStatus(presence.getEnum("status", OnlineStatus.class));

        for (String key : Configuration.getConfig().getSection("settings.presence.activities").singleLayerKeySet()) {
            FlatFileSection section = Configuration.getConfig().getSection("settings.presence.activities." + key);
            this.activities.add(Activity.of(section.getEnum("type", Activity.ActivityType.class), section.getString("name"), section.getString("url")));
        }

        // start the timer.
        List<Guild> guilds = jda.getGuilds();
        SpotTimer.schedule(s -> {
            Activity activity = activities.get(new Random().nextInt(activities.size()));
            int members = guilds.stream().mapToInt(x -> x.getMembers().size()).sum();
            this.jda.getPresence().setActivity(Activity.of(activity.getType(), activity.getName().replace("%members%", Utils.formatNumber(members)), activity.getUrl()));
        }, 0, 1000 * presence.getLong("activity-time"));
    }

    public void channelCountUpdater() {
        SpotTimer.schedule(s -> {
            VoiceChannel voiceChannel = this.jda.getVoiceChannelById(Configuration.getConfig().getLong("settings.member-count.channel-id"));
            if (voiceChannel == null) return;

            int members = voiceChannel.getGuild().getMembers().size();
            voiceChannel.getManager().setName(Configuration.getConfig().getString("settings.member-count.channel-name").replace("%members%", Utils.formatNumber(members))).queue();
        }, 0, 1000 * 5);
    }

    public void load() {
        this.statusUpdater();
        this.channelCountUpdater();

        // load all the reaction roles.
        FlatFileSection reactionRoles = Configuration.getConfig().getSection("settings.reactions");
        for (String key : reactionRoles.singleLayerKeySet()) {
            FlatFileSection section = reactionRoles.getSection(key);
            SpotReaction reactionRole = new SpotReaction(key, section);
            TextChannel channel = reactionRole.getChannel();
            if (channel == null) continue;

            reactionRole.getMessage(message -> {
                SpotMessage spotMessage = reactionRole.getSpotMessage();
                if (message == null) {
                    spotMessage.sendMessage(channel).setActionRows(ActionRow.of(reactionRole.getSelectionMenu())).queue(m -> {
                        Configuration.getStaticData().set("data.reactions." + key + ".message-id", m.getId());
                        reactionRole.setMessageID(m.getId());
                    });
                    return;
                }
                spotMessage.editMessage(message).setActionRows(ActionRow.of(reactionRole.getSelectionMenu())).queue();
            });
        }

        // load the ticket dropdown.
        FlatFileSection ticketDropdown = Configuration.getConfig().getSection("settings.ticket");
        this.ticketCreator = new TicketCreator(ticketDropdown);
        if (this.ticketCreator.getChannel() == null) return;

        ticketCreator.getMessage(message -> {
            SpotMessage spotMessage = ticketCreator.getSpotMessage();
            if (message == null) {
                spotMessage.sendMessage(ticketCreator.getChannel()).setActionRows(ActionRow.of(ticketCreator.getSelectionMenu())).queue(m -> {
                    Configuration.getStaticData().set("ticket.message-id", m.getId());
                    ticketCreator.setMessageID(m.getId());
                });
                return;
            }
            spotMessage.editMessage(message).setActionRows(ActionRow.of(ticketCreator.getSelectionMenu())).queue();
        });
    }

    public Guild getGuild() {
        return jda.getGuildById(Configuration.getConfig().getLong("settings.guild-id"));
    }

    public Role getMuteRole() {
        return this.getGuild()
                .getRoleById(Configuration.getConfig().getLong("settings.punishment.mute-role-id"));
    }

    public TextChannel getLogChannel() {
        return jda.getTextChannelById(Configuration.getConfig().getLong("settings.logging.channel-id"));
    }

    public TicketCreator getTicketCreator() {
        return this.ticketCreator;
    }
}
