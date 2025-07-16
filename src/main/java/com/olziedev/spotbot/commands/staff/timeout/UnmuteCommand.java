package com.olziedev.spotbot.commands.staff.timeout;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.punishment.Punishment;
import com.olziedev.spotbot.server.punishment.PunishmentType;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class UnmuteCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("unmute-command");

    public UnmuteCommand() {
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
        Punishment punishment = SpotBot.getDatabaseManager().getPunishments(Long.parseLong(memberID))
                .stream().filter(x -> x.getPunishmentType() == PunishmentType.MUTE).findFirst().orElse(null);
        if (punishment != null) {
            new SpotMessage(Configuration.getLang().getSection("lang.unmuted-member"), cmd).sendMessage().queue();
            punishment.deletePunishment();
            return;
        }
        Member member = cmd.getGuild().getMemberById(memberID);
        if (member == null) {
            new SpotMessage(Configuration.getLang().getSection("lang.member-not-found"), cmd).sendMessage().queue();
            return;
        }
        if (!member.getRoles().contains(SpotBot.getServer().getMuteRole())) {
            new SpotMessage(Configuration.getLang().getSection("lang.cannot-unmute-member"), cmd).sendMessage().queue();
            return;
        }
        cmd.getGuild().removeRoleFromMember(member, SpotBot.getServer().getMuteRole()).queue();
        new SpotMessage(Configuration.getLang().getSection("lang.unmuted-member"), cmd).sendMessage().queue();
    }
}
