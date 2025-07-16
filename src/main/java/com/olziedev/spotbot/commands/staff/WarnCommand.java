package com.olziedev.spotbot.commands.staff;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.punishment.PunishmentCreator;
import com.olziedev.spotbot.server.punishment.PunishmentType;
import com.olziedev.spotbot.utils.Configuration;
import com.olziedev.spotbot.utils.Utils;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;

public class WarnCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("warn-command");

    public WarnCommand() {
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
        Member member = cmd.getOption("user").getAsMember();
        if (member == null) {
            new SpotMessage(Configuration.getLang().getSection("lang.member-not-found"), cmd).sendMessage().queue();
            return;
        }
        new PunishmentCreator(PunishmentType.WARN, null, cmd.getOption("reason") == null ? null : cmd.getOption("reason").getAsString())
                .create(member.getIdLong(), cmd, false, warn -> {
                    new ActionLog(member, cmd.getMember(), "● Action: **Member Warned**\n● Reason: **" + (warn.getReason() == null ? "N/A" : warn.getReason()) + "**", Color.ORANGE);
                    SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-created-time" + (warn.getReason() == null ? "" : "-reason")), cmd);
                    spotMessage.replaceOptions("%member%", member.getAsMention());
                    spotMessage.replaceOptions("%punishment%", warn.getPunishmentType().getOtherName().toLowerCase());
                    spotMessage.replaceOptions("%reason%", warn.getReason() == null ? "N/A" : warn.getReason());
                    spotMessage.replaceOptions("%time%", warn.getTimeFor() == null ? "N/A" : Utils.formatTime(warn.getTimeFor()));
                    spotMessage.sendMessage().queue();
                });
    }
}
