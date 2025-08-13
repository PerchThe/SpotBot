package com.olziedev.spotbot.commands.staff;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.logs.ActionLog;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.server.punishment.PunishmentCreator;
import com.olziedev.spotbot.server.punishment.PunishmentType;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;

public class KickCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("kick-command");

    public KickCommand() {
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
        new PunishmentCreator(PunishmentType.KICK, null, cmd.getOption("reason") == null ? null : cmd.getOption("reason").getAsString())
                .create(member.getIdLong(), cmd, false, kick -> {
                    new ActionLog(member, cmd.getMember(), "●     @Override\n" +
                            "    public void onExecute(SlashExecutor cmd) {\n" +
                            "        Member member = cmd.getOption(\"user\").getAsMember();\n" +
                            "        if (member == null) {\n" +
                            "            new SpotMessage(Configuration.getLang().getSection(\"lang.member-not-found\"), cmd).sendMessage().queue();\n" +
                            "            return;\n" +
                            "        }\n" +
                            "        new PunishmentCreator(PunishmentType.KICK, null, cmd.getOption(\"reason\") == null ? null : cmd.getOption(\"reason\").getAsString())\n" +
                            "                .create(member.getIdLong(), cmd, false, kick -> {\n" +
                            "                    new ActionLog(member, cmd.getMember(), \"● Action: **Member Kicked**\\n● Reason: **\" + (kick.getReason() == null ? \"N/A\" : kick.getReason()) + \"**\", Color.ORANGE);\n" +
                            "                    SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection(\"lang.punishment-created\" + (kick.getReason() == null ? \"\" : \"-reason\")), cmd);\n" +
                            "                    spotMessage.replaceOptions(\"%member%\", member.getAsMention());\n" +
                            "                    spotMessage.replaceOptions(\"%punishment%\", kick.getPunishmentType().getOtherName().toLowerCase());\n" +
                            "                    spotMessage.replaceOptions(\"%reason%\", kick.getReason() == null ? \"N/A\" : kick.getReason());\n" +
                            "                    spotMessage.sendMessage().queue();\n" +
                            "                });\n" +
                            "    }\n" +
                            "}: **Member Kicked**\n● Reason: **" + (kick.getReason() == null ? "N/A" : kick.getReason()) + "**", Color.ORANGE);
                    SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-created" + (kick.getReason() == null ? "" : "-reason")), cmd);
                    spotMessage.replaceOptions("%member%", member.getAsMention());
                    spotMessage.replaceOptions("%punishment%", kick.getPunishmentType().getOtherName().toLowerCase());
                    spotMessage.replaceOptions("%reason%", kick.getReason() == null ? "N/A" : kick.getReason());
                    spotMessage.sendMessage().queue();
                });
    }
}
