package com.olziedev.spotbot.commands.staff.ban;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.punishment.Punishment;
import com.olziedev.spotbot.server.punishment.PunishmentType;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UnbanCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("unban-command");

    public UnbanCommand() {
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
        String memberID = cmd.getOption("user").getAsString();
        cmd.getGuild().unban(memberID).queue(success -> {
            SpotBot.getDatabaseManager().getPunishments(Long.parseLong(memberID))
                    .stream().filter(x -> x.getPunishmentType() == PunishmentType.BAN).findFirst().ifPresent(Punishment::deletePunishment);

            new SpotMessage(Configuration.getLang().getSection("lang.unbanned-member"), cmd).sendMessage().queue();
        }, error -> {
            new SpotMessage(Configuration.getLang().getSection("lang.cannot-unban-member"), cmd).sendMessage().queue();
        });
    }
}
