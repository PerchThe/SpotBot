package com.olziedev.spotbot.commands.general;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import com.olziedev.spotbot.utils.Utils;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class StealEmojiCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("stealemoji-command");

    private final HashMap<Long, Long> cooldowns;
    private final int COOLDOWN;

    public StealEmojiCommand() {
        super(commands.getString("command"));
        this.setDescription(commands.getString("description"));
        this.setRoles(commands.getStringList("roles").stream().map(Long::parseLong).toArray(Long[]::new));

        for (String option : commands.getSection("options").singleLayerKeySet()) {
            FlatFileSection options = commands.getSection("options." + option);
            this.addOption(new OptionData(options.getEnum("type", OptionType.class), options.getString("name"), options.getString("description"), options.getBoolean("required")) {{
                FlatFileSection choices = options.getSection("choices");
                if (choices != null) {
                    for (String choice : choices.singleLayerKeySet()) {
                        this.addChoice(choice, choices.getString(choice));
                    }
                }
            }});
        }
        this.cooldowns = new HashMap<>();
        COOLDOWN = commands.getInt("cooldown");
    }

    @Override
    public void onExecute(SlashExecutor cmd) {
        Long timer = this.cooldowns.get(cmd.getMember().getIdLong());
        if (timer != null) {
            SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.emote-cooldown"), cmd);
            spotMessage.replaceOptions("%time%", Utils.formatTime((COOLDOWN + 1) - ((new Date().getTime() / 1000) - timer)));
            spotMessage.sendMessage().queue();
            return;
        }
        Matcher matcher = Message.MentionType.EMOTE.getPattern().matcher(cmd.getOption("emoji").getAsString());
        if (!matcher.matches()) {
            new SpotMessage(Configuration.getLang().getSection("lang.not-valid-emote"), cmd).sendMessage().queue();
            return;
        }
        String url = String.format(Emote.ICON_URL, Long.parseUnsignedLong(matcher.group(2)), (cmd.getOption("emoji").getAsString().startsWith("<a:") ? "gif" : "webp") + "?size=80&quality=lossless");
        new SpotMessage(Configuration.getLang().getSection("lang.emote-wait"), cmd).sendMessage(ReplyAction.class).queue(replyAction -> {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                cmd.getGuild().createEmote(cmd.getOption("name").getAsString().replace(" ", "_"), Icon.from(httpURLConnection.getInputStream())).queue(success -> {
                    SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.emote-upload"), cmd);
                    spotMessage.replaceOptions("%name%", success.getName());
                    spotMessage.replaceOptions("%emote%", success.getAsMention());
                    spotMessage.sendMessage(replyAction).queue();
                    new ActionLog(cmd.getMember(), cmd.getMember(), "● Action: **Emote added**\n● Name: **" + success.getName() + "**\n● Emote: **" + success.getAsMention() + "**", Color.ORANGE);

                    this.cooldowns.put(cmd.getMember().getIdLong(), new Date().getTime() / 1000);
                    SpotBot.getExecutor().schedule(() -> this.cooldowns.remove(cmd.getMember().getIdLong()), 1000L * COOLDOWN, TimeUnit.MILLISECONDS);
                }, error -> {
                    SpotBot.getLogger().info("Error uploading emote: " + error.getMessage());
                    new SpotMessage(Configuration.getLang().getSection("lang.emote-upload-failed"), cmd).sendMessage(replyAction).queue();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                new SpotMessage(Configuration.getLang().getSection("lang.not-valid-emote"), cmd).sendMessage(replyAction).queue();
            }
        });
    }
}
