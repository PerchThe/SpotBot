package com.olziedev.spotbot.events.listeners;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

public class LeaveEvent extends SpotEvent {

    private final long greetingChannel = Configuration.getConfig().getLong("settings.greeting.leave.channel-id");

    public LeaveEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        TextChannel channel = this.jda.getTextChannelById(this.greetingChannel);
        if (channel == null) return;

        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.greeting.leave.message"), null);
        spotMessage.replaceOptions("%guild%", event.getGuild().getName());
        spotMessage.replaceOptions("%member%", event.getMember().getAsMention());
        spotMessage.replaceOptions("%member_tag%", event.getMember().getUser().getAsTag());
        if (spotMessage.hasEmbed()) {
            spotMessage.getSpotEmbed().replaceOptions("%guild%", event.getGuild().getName());
            spotMessage.getSpotEmbed().replaceOptions("%member%", event.getMember().getAsMention());
            spotMessage.getSpotEmbed().replaceOptions("%member_tag%", event.getMember().getUser().getAsTag());
        }
        spotMessage.sendMessage(channel).queue();
    }
}
