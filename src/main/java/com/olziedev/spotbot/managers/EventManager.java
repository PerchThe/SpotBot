package com.olziedev.spotbot.managers;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.events.SpotEvent;
import net.dv8tion.jda.api.JDA;
import org.reflections.Reflections;

public class EventManager extends Manager {

    private final Reflections reflections;

    public EventManager(JDA jda, SpotBot spotBot) {
        super(jda, spotBot);
        this.reflections = new Reflections("com.olziedev.spotbot");
    }

    @Override
    public void setup() {
        try {
            DatabaseManager manager = SpotBot.getDatabaseManager();
            for (Class<? extends SpotEvent> event : this.reflections.getSubTypesOf(SpotEvent.class)) {
                SpotEvent spotEvent = event.getDeclaredConstructor(JDA.class, DatabaseManager.class).newInstance(this.jda, manager);

                SpotBot.getLogger().info("Registering event: " + spotEvent.getClass().getSimpleName());
                this.jda.addEventListener(spotEvent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void load() {

    }


}
