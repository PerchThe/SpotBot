package com.olziedev.spotbot.commands.general.ticket;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.ticket.Ticket;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;

public class AddCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("add-command");

    public AddCommand() {
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
        Member member = cmd.getOption("user").getAsMember();
        if (member == null) {
            new SpotMessage(Configuration.getLang().getSection("lang.member-not-found"), cmd).sendMessage().queue();
            return;
        }
        TextChannel channel = ticket.getTextChannel();
        if (channel.getMembers().contains(member)) {
            new SpotMessage(Configuration.getLang().getSection("lang.member-already-added"), cmd).sendMessage().queue();
            return;
        }
        channel.getManager().putMemberPermissionOverride(member.getIdLong(), SpotBot.getServer().getTicketCreator().getPermissions(), new ArrayList<>()).queue();
        SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.member-added"), cmd);
        spotMessage.replaceOptions("%member%", member.getAsMention());
        spotMessage.sendMessage().queue();
    }
}
