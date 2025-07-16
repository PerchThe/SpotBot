package com.olziedev.spotbot.events;

import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public abstract class SpotEvent extends ListenerAdapter {

    public final JDA jda;

    public final DatabaseManager manager;
    protected final List<String> blacklistedIds;

    public SpotEvent(JDA jda, DatabaseManager manager) {
        this.jda = jda;
        this.manager = manager;
        this.blacklistedIds = Configuration.getConfig().getStringList("settings.logging.blacklisted-channels");
    }

    public boolean isBlacklisted(ISnowflake textChannel) {
        return blacklistedIds.contains(textChannel.getId());
    }
}
