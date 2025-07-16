package com.olziedev.spotbot.commands.general.suggest;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.suggestion.Suggestion;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class SuggestCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("suggestion-command");


    public SuggestCommand() {
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
    }

    @Override
    public void onExecute(SlashExecutor cmd) {
        Suggestion.create(cmd.getOption("suggestion").getAsString(), cmd.getMember(), suggestion -> {
            if (suggestion == null) {
                new SpotMessage(Configuration.getLang().getSection("lang.suggestion-id-not-found"), cmd).sendMessage().queue();
                return;
            }
            SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.suggestion-created"), cmd);
            spotMessage.replaceOptions("%link%", String.format("https://discord.com/channels/%s/%s/%s", cmd.getGuild().getId(), Configuration.getConfig().getLong("settings.suggestion.channel-id"), suggestion.getMessageID()));
            spotMessage.sendMessage().queue();
        });
    }
}
