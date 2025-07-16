package com.olziedev.spotbot.commands.staff;

import com.olziedev.olziecommand.framework.api.slash.SlashCommand;
import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class PurgeCommand extends SlashCommand {

    private static final FlatFileSection commands = Configuration.getCommands().getSection("purge-command");

    public PurgeCommand() {
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
        long amount = cmd.getOption("amount").getAsLong();
        if (amount <= 0 || amount > 100) {
            new SpotMessage(Configuration.getLang().getSection("lang.purge-invalid-amount"), cmd).sendMessage().queue();
            return;
        }
        cmd.getChannel().getHistory().retrievePast((int) amount).queue(messages -> {
            if (messages.size() == 1) {
                messages.get(0).delete().queue();
                return;
            }
            messages.removeIf(m -> m.getTimeCreated().isBefore(OffsetDateTime.now().minus(2, ChronoUnit.WEEKS)));
            if (messages.isEmpty()) {
                new SpotMessage(Configuration.getLang().getSection("lang.purge-no-messages"), cmd).sendMessage().queue();
                return;
            }
            cmd.getChannel().deleteMessages(messages).queue();
            new SpotMessage(Configuration.getLang().getSection("lang.purged-amount"), cmd).replaceOptions("%amount%", String.valueOf(amount)).sendMessage().queue();
        });
    }
}
