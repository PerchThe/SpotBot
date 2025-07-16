package com.olziedev.spotbot.commands.general.ticket;

import com.olziedev.olziecommand.framework.action.CommandActionType;
import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.ticket.Ticket;
import com.olziedev.spotbot.server.ticket.TicketCategory;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ForceCloseCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("forceclose-command");

    public ForceCloseCommand() {
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
        DatabaseManager manager = SpotBot.getDatabaseManager();
        Ticket ticket = manager.getTicket(cmd.getChannel());
        if (ticket == null) {
            new SpotMessage(Configuration.getLang().getSection("lang.not-in-ticket"), cmd).sendMessage().queue();
            return;
        }
        TicketCategory category = ticket.getCategory();
        if (category == null) return;

        if (category.noPermission(cmd.getMember())) {
            this.olzieCommand.getActionRegister().executeAction(CommandActionType.CMD_NO_PERMISSION, cmd);
            return;
        }
        ticket.close(cmd.getMember(), System.currentTimeMillis() / 1000);
    }
}
