package com.olziedev.spotbot.server.punishment;

import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import com.olziedev.spotbot.utils.Utils;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.function.Consumer;

public class PunishmentCreator {

    private final PunishmentType punishmentType;
    private final Long timeEnd;
    private final String reason;

    private final DatabaseManager manager;

    public PunishmentCreator(PunishmentType punishmentType, Long timeEnd, String reason) {
        this.manager = SpotBot.getDatabaseManager();
        this.punishmentType = punishmentType;
        Long time = punishmentType == PunishmentType.WARN ? Long.valueOf(Configuration.getConfig().getLong("settings.punishment.warning.default-time")) : timeEnd;

        this.timeEnd = time == null || time <= 0 ? null : time * 1000;
        this.reason = reason;
    }

    public PunishmentCreator(FlatFileSection section) {
        this.manager = SpotBot.getDatabaseManager();
        this.punishmentType = section.getEnum("type", PunishmentType.class);
        this.reason = section.getString("reason");

        long time = punishmentType == PunishmentType.WARN ? Configuration.getConfig().getLong("settings.punishment.warning.default-time") : Utils.parseShortTime(section.getString("time"));
        this.timeEnd = time <= 0 ? null : time * 1000;
    }

    public void create(long user, SlashExecutor cmd, boolean pending) {
        this.create(user, cmd, pending, null);
    }

    public void create(long user, SlashExecutor cmd, boolean pending, Consumer<Punishment> callback) {
        Member executor = (cmd.getMember() == null ? cmd.getGuild().getSelfMember() : cmd.getMember());
        TextChannel textChannel = cmd.getChannel();

        Member userMember = cmd.getGuild().getMemberById(user);
        String punishmentName = this.punishmentType.getName().toLowerCase();
        SpotBot.getLogger().info("Punishing user " + userMember.getEffectiveName() + " with " + punishmentName + " for " + this.reason);
        if (this.punishmentType != PunishmentType.WARN && !pending && !cmd.getGuild().getSelfMember().canInteract(userMember)) {
            if (textChannel != null && cmd.getMember() != null) {
                SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-higher-me"), cmd);
                spotMessage.replaceOptions("%punishment%", punishmentName);
                spotMessage.sendMessage().queue();
            }
            SpotBot.getLogger().info("Punishing cannot be executed, the user is higher than me!");
            return;
        }
        if (this.punishmentType == PunishmentType.WARN && cmd.getGuild().getSelfMember().getIdLong() == user) {
            if (textChannel != null && cmd.getMember() != null) {
                SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-self"), cmd);
                spotMessage.replaceOptions("%punishment%", punishmentName);
                spotMessage.sendMessage().queue();
            }
            SpotBot.getLogger().info("Punishing cannot be executed, why you trying to punish me?");
            return;
        }
        if (!pending && !executor.canInteract(userMember)) {
            if (textChannel != null && cmd.getMember() != null) {
                SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-higher-you"), cmd);
                spotMessage.replaceOptions("%punishment%", punishmentName);
                spotMessage.sendMessage().queue();
            }
            SpotBot.getLogger().info("Punishing cannot be executed, the user is higher than " + executor.getEffectiveName() + "!");
            return;
        }
        Date date = new Date();

        Runnable action = () -> {
            //TODO: send a message here before in their dms the user is punished?
            switch (this.punishmentType) {
                case BAN:
                    cmd.getGuild().ban(userMember, 0, reason).queue(success -> {}, error -> {});
                    break;
                case MUTE:
                    cmd.getGuild().addRoleToMember(userMember, SpotBot.getServer().getMuteRole()).queue(success -> {}, error -> {});
                    break;
                case KICK:
                    cmd.getGuild().kick(userMember, reason).queue(success -> {}, error -> {});
            }
        };
        this.create(x -> {
            if (this.punishmentType == PunishmentType.WARN) {
                long warns = this.manager.getPunishments(user).stream().filter(y -> y.getPunishmentType() == PunishmentType.WARN && y.getTime() != null).count();
                FlatFileSection section = Configuration.getConfig().getSection("settings.punishment.warning.warnings");
                if (section.get(String.valueOf(warns)) == null) {
                    long largesWarn = section.singleLayerKeySet().stream().mapToLong(Long::parseLong).max().orElse(0L);
                    warns = warns < largesWarn ? -1 : largesWarn;
                }
                if (warns != -1) {
                    SpotBot.getLogger().info("Adding a warning punishment, warns: " + warns);
                    new PunishmentCreator(section.getSection(String.valueOf(warns))).create(user, cmd, false, warn -> {
                        SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-created" + (cmd.getMember() == null ? "-bot" : "-") + "time" + (cmd.getMember() == null || warn.getReason() == null ? "" : "-reason")), cmd);
                        spotMessage.replaceOptions("%member%", userMember.getAsMention());
                        spotMessage.replaceOptions("%punishment%", warn.getPunishmentType().getOtherName().toLowerCase());
                        spotMessage.replaceOptions("%reason%", warn.getReason());
                        spotMessage.replaceOptions("%time%", warn.getTimeFor() == null ? "N/A" : Utils.formatTime(warn.getTimeFor()));
                        spotMessage.sendMessage().queue();
                    });
                    action.run();
                    return;
                }
            }
            if (cmd.getMember() == null) {
                if (callback != null) callback.accept(x);
                SpotMessage spotMessage = new SpotMessage(Configuration.getLang().getSection("lang.punishment-created-bot" + (this.timeEnd == null ? "" : "-time")), cmd);
                spotMessage.replaceOptions("%member%", userMember.getAsMention());
                spotMessage.replaceOptions("%punishment%", this.punishmentType.getOtherName().toLowerCase());
                spotMessage.replaceOptions("%reason%", this.reason);
                spotMessage.replaceOptions("%time%", this.timeEnd == null ? "N/A" : Utils.formatTime(this.timeEnd / 1000));
                spotMessage.sendMessage().queue();
                action.run();
                return;
            }
            if (callback == null) {
                action.run();
                return;
            }
            callback.accept(x);
            action.run();
        }, this.timeEnd == null ? null : date.getTime() + this.timeEnd, date, user, executor, pending);
    }

    private void create(Consumer<Punishment> callback, Long timeEnd, Date date, long user, Member executor, boolean pending) {
        // remove the old same type of punishment if they had one.
        if (this.punishmentType != PunishmentType.WARN) {
            manager.getPunishments(user).stream().filter(x -> x.getTime() != null && x.getPunishmentType() == this.punishmentType).findFirst().ifPresent(Punishment::deletePunishment);
        }
        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO spot_punishments(user_id, type, time, time_for, time_on, reason, executor, pending) VALUES(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, user);
            ps.setString(2, this.punishmentType.name());
            ps.setString(3, timeEnd == null ? null : String.valueOf(timeEnd));
            ps.setString(4, timeEnd == null ? null : String.valueOf((timeEnd - date.getTime()) / 1000));
            ps.setLong(5, date.getTime());
            ps.setString(6, this.reason);
            ps.setLong(7, executor.getIdLong());
            ps.setBoolean(8, pending);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Punishment punishment = new Punishment(rs.getLong(1));
                this.manager.addPunishment(punishment);
                if (callback != null) callback.accept(punishment);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
