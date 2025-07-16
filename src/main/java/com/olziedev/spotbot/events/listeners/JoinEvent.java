package com.olziedev.spotbot.events.listeners;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.punishment.Punishment;
import com.olziedev.spotbot.server.punishment.PunishmentType;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.NotNull;

public class JoinEvent extends SpotEvent {

    private final long greetingChannel = Configuration.getConfig().getLong("settings.greeting.welcome.channel-id");

    public JoinEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        this.giveRole(event.getMember());
        Punishment mutePending = manager.getPunishments(event.getMember().getIdLong()).stream().filter(x -> x.getPunishmentType() == PunishmentType.MUTE).findAny().orElse(null);
        if (mutePending != null) {
            event.getGuild().addRoleToMember(event.getMember(), SpotBot.getServer().getMuteRole()).queue();
            mutePending.setPending(false);
            return;
        }
        TextChannel channel = this.jda.getTextChannelById(this.greetingChannel);
        if (channel == null) return;

        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.greeting.welcome.message"), null);
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

    private void giveRole(Member member) {
        for (String id : Configuration.getConfig().getStringList("settings.greeting.welcome.given-roles")) {
            member.getGuild().addRoleToMember(member, jda.getRoleById(id)).queue();
        }
    }
}
