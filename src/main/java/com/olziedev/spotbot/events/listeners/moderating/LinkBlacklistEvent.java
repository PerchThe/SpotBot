package com.olziedev.spotbot.events.listeners.moderating;

import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.server.punishment.PunishmentCreator;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkBlacklistEvent extends SpotEvent {

    private final Pattern URL_PATTERN = Pattern.compile("((http|ftp|https):\\/\\/)(([\\w.-]*)\\.([\\w]*))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private final List<String> blacklisted = Configuration.getConfig().getStringList("settings.auto-moderation.blacklisted-links.list");
    private final PunishmentCreator punishmentCreator = new PunishmentCreator(Configuration.getConfig().getSection("settings.auto-moderation.blacklisted-links.punishment"));

    public LinkBlacklistEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        this.event(event.getMember(), event.getMessage(), event.getChannel());
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        this.event(event.getMember(), event.getMessage(), event.getChannel());
    }

    private void event(Member member, Message message, TextChannel channel) {
        String content = message.getContentRaw();
        Matcher matches = URL_PATTERN.matcher(content);
        while (matches.find()) {
            String url = matches.group().replace("https://www.", "").replace("http://www.", "").replace("http//", "").replace("https://", "");
            if (!blacklisted.contains(url)) continue;

            this.punishmentCreator.create(member.getIdLong(), null, false, x -> message.delete().queue());
            return;
        }
    }
}