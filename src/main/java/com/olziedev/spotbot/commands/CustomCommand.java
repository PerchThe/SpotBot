package com.olziedev.spotbot.commands;

import com.olziedev.olziecommand.OlzieCommand;
import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class CustomCommand {

    private final OlzieCommand olzieCommand;

    public CustomCommand(OlzieCommand olzieCommand) {
        this.olzieCommand = olzieCommand;
    }

    public void load(List<CommandData> data) {
        for (String key : Configuration.getCustomCommands().singleLayerKeySet()) {
            FlatFileSection commands = Configuration.getCustomCommands().getSection(key);
            SlashCommand slashCommand = new SlashCommand(commands.getString("command")) {
                @Override
                public void onExecute(SlashExecutor slashExecutor) {
                    SpotMessage spotMessage = new SpotMessage(commands.getSection("message"), slashExecutor);
                    spotMessage.replaceOptions(this.getOptions());
                    if (spotMessage.hasEmbed()) spotMessage.getSpotEmbed().replaceOptions(this.getOptions());

                    spotMessage.sendMessage().queue();
                }
            };
            slashCommand.setDescription(commands.getString("description"));
            slashCommand.setRoles(commands.getStringList("roles").stream().map(Long::parseLong).toArray(Long[]::new));

            for (String option : commands.getSection("options").singleLayerKeySet()) {
                FlatFileSection options = commands.getSection("options." + option);
                slashCommand.addOption(new OptionData(options.getEnum("type", OptionType.class), options.getString("name"), options.getString("description"), options.getBoolean("required")) {{
                    FlatFileSection choices = options.getSection("choices");
                    if (choices != null) {
                        for (String choice : choices.singleLayerKeySet()) {
                            this.addChoice(choice, choices.getString(choice));
                        }
                    }
                }});
            }
            CommandData commandData = new CommandData(slashCommand.getName(), slashCommand.getDescription());
            commandData.addOptions(slashCommand.getOptions());
            data.add(commandData);
            olzieCommand.getCommands().add(slashCommand);
        }
    }
}
