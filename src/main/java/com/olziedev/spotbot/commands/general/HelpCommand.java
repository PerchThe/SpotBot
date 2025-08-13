package com.olziedev.spotbot.commands.general;

import com.olziedev.olziecommand.framework.api.FrameworkCommand;
import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class HelpCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("help-command");

    public HelpCommand() {
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
        SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.help-menu"), cmd);
        if (spotMessage.hasEmbed()) {
            OptionMapping command = cmd.getOption("command");
            for (FrameworkCommand cmds : olzieCommand.getCommands()) {
                if (olzieCommand.noPermission(cmds, cmd.getMember(), cmd.getMember().getUser())) continue;
                if (command != null && (!cmds.getName().startsWith(command.getAsString()) && !cmds.getName().contains(command.getAsString())))
                    continue;

                spotMessage.getSpotEmbed().getEmbedBuilder().addField(olzieCommand.getPrefix() + cmds.getName(), cmds.getDescription(), true);
            }
        }
        spotMessage.sendMessage().queue();
    }
}
