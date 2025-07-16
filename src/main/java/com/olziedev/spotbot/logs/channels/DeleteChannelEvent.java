package com.olziedev.spotbot.logs.channels;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.StoreChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;

import java.awt.*;

public class DeleteChannelEvent extends SpotEvent {

    public DeleteChannelEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        TextChannel channel = event.getChannel();
        new ActionLog(null, "● Action: **Text Channel Deleted**\n● Channel: **" + channel.getName() + "**\n", Color.ORANGE);
    }

    @Override
    public void onStoreChannelDelete(StoreChannelDeleteEvent event) {
        StoreChannel channel = event.getChannel();
        new ActionLog(null, "● Action: **Store Channel Deleted**\n● Channel: **" + channel.getName() + "**\n", Color.ORANGE);
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        VoiceChannel channel = event.getChannel();
        new ActionLog(null, "● Action: **Voice Channel Deleted**\n● Channel: **" + channel.getName() + "**\n", Color.ORANGE);
    }

    @Override
    public void onCategoryDelete(CategoryDeleteEvent event) {
        Category channel = event.getCategory();
        new ActionLog(null, "● Action: **Category Channel Deleted**\n● Channel: **" + channel.getName() + "**\n", Color.ORANGE);
    }
}
