package com.olziedev.spotbot;

import com.olziedev.olziecommand.OlzieCommand;
import com.olziedev.olziecommand.framework.action.CommandActionType;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.olziecomponent.OlzieComponent;
import com.olziedev.spotbot.commands.CustomCommand;
import com.olziedev.spotbot.events.waiter.EventWaiter;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.managers.EventManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.SpotServer;
import com.olziedev.spotbot.utils.Configuration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public class SpotBot extends ListenerAdapter {

    private static SpotBot instance;

    private JDA jda;
    private final Logger logger = JDALogger.getLog("SpotBot");
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    private DatabaseManager databaseManager;
    private SpotServer server;
    private OlzieComponent olzieComponent;
    private EventWaiter eventWaiter;
    public SpotBot() {
        new Configuration().load(getDataFolder());

        try {
            // Build JDA
            JDABuilder jdaBuilder = JDABuilder.create(Configuration.getConfig().getString("settings.bot-token"),
                            GUILD_MEMBERS,
                            GUILD_BANS,
                            GUILD_WEBHOOKS,
                            GUILD_INVITES,
                            GUILD_VOICE_STATES,
                            GUILD_MESSAGES,
                            GUILD_MESSAGE_REACTIONS,
                            DIRECT_MESSAGES,
                            GUILD_PRESENCES)
                    .addEventListeners(this)
                    .disableCache(CacheFlag.EMOTE);
            jda = jdaBuilder.build();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.logger.info(this.jda.getSelfUser().getName() + " is online, currently in " + this.jda.getGuilds().size() + " servers.");
        EnumSet<Message.MentionType> deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE);
        MessageAction.setDefaultMentions(EnumSet.complementOf(deny));
        if (!MessageAction.getDefaultMentions().containsAll(EnumSet.complementOf(deny))) return;

//        event.getJDA().updateCommands().complete();
        OlzieCommand olzieCommand = new OlzieCommand(this.jda, getClass(), null, this.logger)
                .getActionRegister()
                .registerAction(CommandActionType.CMD_NO_PERMISSION, cmd -> {
                    new SpotMessage(Configuration.getLang().getSection("lang.no-permission"), ((SlashExecutor) cmd)).sendMessage(ReplyAction.class)
                            .setEphemeral(true)
                            .queue();
                })
                .buildActions();
        olzieCommand.registerCommands(Collections.emptyList(), x -> new CustomCommand(olzieCommand).load(x), jda.getGuildById(Configuration.getConfig().getLong("settings.guild-id")));
        this.olzieComponent = new OlzieComponent(this.jda);
        this.eventWaiter = new EventWaiter();

        this.databaseManager = new DatabaseManager(this.jda, this);
        this.databaseManager.setup();
        this.databaseManager.load(() -> {
            EventManager eventManager = new EventManager(this.jda, this);
            eventManager.setup();
            eventManager.load();

            this.server = new SpotServer(this.jda);
            this.server.load();
        });
    }

    public static void main(String[] args) {
        instance = new SpotBot();
    }

    public static JDA getJDA() {
        return instance.jda;
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static ScheduledExecutorService getExecutor() {
        return instance.executor;
    }

    public static DatabaseManager getDatabaseManager() {
        return instance.databaseManager;
    }

    public static SpotServer getServer() {
        return instance.server;
    }

    public static OlzieComponent getOlzieComponent() {
        return instance.olzieComponent;
    }

    public static EventWaiter getEventWaiter() {
        return instance.eventWaiter;
    }

    public static File getDataFolder() {
        return new File("SpotBot");
    }
}
