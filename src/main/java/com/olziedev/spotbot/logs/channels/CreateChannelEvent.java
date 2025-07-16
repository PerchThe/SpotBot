package com.olziedev.spotbot.logs.channels;

import com.olziedev.spotbot.events.SpotEvent;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.managers.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.StoreChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.category.CategoryCreateEvent;
import net.dv8tion.jda.api.events.channel.store.StoreChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;

import java.awt.*;

public class CreateChannelEvent extends SpotEvent {

    public CreateChannelEvent(JDA jda, DatabaseManager manager) {
        super(jda, manager);
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event) {
        TextChannel channel = event.getChannel();
        new ActionLog(null, "● Action: **Text Channel Created**\n● Channel: **" + channel.getAsMention() + "**\n", Color.ORANGE);
    }

    @Override
    public void onStoreChannelCreate(StoreChannelCreateEvent event) {
        StoreChannel channel = event.getChannel();
        new ActionLog(null, "● Action: **Store Channel Created**\n● Channel: **" + channel.getAsMention() + "**\n", Color.ORANGE);
    }

    @Override
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
        VoiceChannel channel = event.getChannel();
        new ActionLog(null, "● Action: **Voice Channel Created**\n● Channel: **" + channel.getName() + "**\n", Color.ORANGE);
    }

    @Override
    public void onCategoryCreate(CategoryCreateEvent event) {
        Category channel = event.getCategory();
        new ActionLog(null, "● Action: **Category Channel Created**\n● Channel: **" + channel.getName() + "**\n", Color.ORANGE);
    }
}
