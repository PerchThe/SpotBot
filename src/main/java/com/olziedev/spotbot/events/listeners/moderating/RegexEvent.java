package com.olziedev.spotbot.events.listeners.moderating;

import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.server.punishment.PunishmentCreator;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexEvent extends SpotEvent {

    private final Map<Pattern, PunishmentCreator> regex = new ConcurrentHashMap<>();

    public RegexEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);

        FlatFileSection section = Configuration.getConfig().getSection("settings.auto-moderation.custom-regex");
        for (String key : section.singleLayerKeySet()) {
            String regex = section.getString(key + ".regex");
            PunishmentCreator punishment = new PunishmentCreator(section.getSection(key + ".punishment"));
            this.regex.put(Pattern.compile(regex), punishment);
        }
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
        for (Map.Entry<Pattern, PunishmentCreator> pattern : regex.entrySet()) {
            Matcher matches = pattern.getKey().matcher(content);
            if (!matches.find()) continue;

            pattern.getValue().create(member.getIdLong(), new SlashExecutor(null, channel, null, channel.getGuild(), null, null, null, null), false, x -> message.delete().queue());
            return;
        }
    }
}
