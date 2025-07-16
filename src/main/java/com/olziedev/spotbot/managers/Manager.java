package com.olziedev.spotbot.managers;

import com.olziedev.spotbot.SpotBot;
import net.dv8tion.jda.api.JDA;

public abstract class Manager {

    public final JDA jda;
    public final SpotBot spotBot;

    public Manager(JDA jda, SpotBot spotBot) {
        this.jda = jda;
        this.spotBot = spotBot;
    }

    public void load() {}

    public void load(Runnable runnable) {

    }

    public abstract void setup();

    public void close() {} // default
}
