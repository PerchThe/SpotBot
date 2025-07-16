package com.olziedev.spotbot.server.punishment;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.server.SpotServer;
import com.olziedev.spotbot.utils.Callback;
import com.olziedev.spotbot.utils.SpotTimer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Punishment implements Comparable<Punishment> {

    private final long id;
    private long userID;
    private PunishmentType punishmentType;
    private Long time;
    private Integer timeFor;
    private Date timeOn;
    private String reason;
    private long executorID;
    private User user;
    private boolean pending;

    private final DatabaseManager manager;

    public Punishment(long id) {
        this.manager = SpotBot.getDatabaseManager();
        this.id = id;

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM spot_punishments WHERE id = ?");
            ps.setLong(1, this.id);
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                this.userID = result.getLong("user_id");
                this.punishmentType = PunishmentType.valueOf(result.getString("type"));
                this.pending = result.getBoolean("pending");
                String time = result.getString("time");
                this.time = time == null || pending ? null : Long.parseLong(time);
                String timeFor = result.getString("time_for");
                this.timeFor = timeFor == null ? null : Integer.parseInt(timeFor);
                this.timeOn = new Date(result.getLong("time_on"));
                this.reason = result.getString("reason");
                this.executorID = result.getLong("executor");
            }
            SpotBot.getExecutor().schedule(this::updater, 500, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public long getID() {
        return this.id;
    }

    public PunishmentType getPunishmentType() {
        return this.punishmentType;
    }

    public long getUserID() {
        return this.userID;
    }

    public void getUser(Callback<User> callback) {
        manager.getJDA().retrieveUserById(this.userID).queue(callback::call, error -> {});
    }

    public Long getTime() {
        return this.time;
    }

    public Integer getTimeFor() {
        return this.timeFor;
    }

    public Date getTimeOn() {
        return this.timeOn;
    }

    public String getReason() {
        return this.reason;
    }

    public boolean isPending() {
        return this.pending;
    }

    public void getExecutor(Callback<User> callback) {
        manager.getJDA().retrieveUserById(this.executorID).queue(callback::call, error -> {});
    }

    public User getExecutor() {
        if (this.user != null) return this.user;

        try {
            return this.user = manager.getJDA().retrieveUserById(this.executorID).complete();
        } catch (Throwable throwable) {
            return null;
        }
    }

    public long getExecutorID() {
        return this.executorID;
    }

    private void updater() {
        if (this.pending) return;

        if (this.time == null) {
            this.deletePunishment();
            return;
        }

        this.getUser(user -> {
            SpotBot.getLogger().info("Proceeding punishment update on " + user.getAsTag());
            SpotTimer.schedule(() -> {
                if (this.time == null) return;

                SpotBot.getLogger().info("Punishment expired on " + user.getAsTag());

                this.executeAction(user);
//                this.expire();
                this.deletePunishment();

                this.getExecutor(executor -> user.openPrivateChannel().queue(channel -> {
                    //TODO: send a message here in dms?
//                    SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy hh:mm a '(GMT)'");
//                    sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT", ZoneId.SHORT_IDS)));
//
//                    String punishString = this.punishmentType == PunishmentType.WARN ? "Warned" : "Muted";
//                    EmbedBuilder eb = new EmbedBuilder()
//                            .setTitle("Punishment Expired")
//                            .setDescription("Your punishment has **EXPIRED** from **" + OlzieBot.getGuild().getName() + "**! To make sure you don't break the rules again, please read the rules <#568502018566520833>! Here is some information.")
//                            .addField("**Information:**", "● Type: **" + this.punishmentType.getName() + "**\n● Reason: **" + (this.reason == null ? "N/A" : reason) + "**\n● Executor: **" + executor.getAsTag() + " (" + executor.getIdLong() + ")**\n● " + punishString + " for: **" + Utils.formatTime(this.timeFor) + "**\n● " + punishString + " on: **" + sdf.format(this.timeOn.getTime()) + "**", false)
//                            .setColor(Color.RED)
//                            .setTimestamp(Instant.now())
//                            .setThumbnail(manager.getJDA().getSelfUser().getEffectiveAvatarUrl())
//                            .setFooter("Made by " + OlzieBot.getOlzieUser().getAsTag(), OlzieBot.getOlzieUser().getEffectiveAvatarUrl());
//                    channel.sendMessageEmbeds(eb.build()).queue(success -> {}, error -> {});
                }, error -> {}));
            }, new Date(this.time));
        });
    }

    private void executeAction(User user) {
        SpotServer spotServer = SpotBot.getServer();
        Guild guild = spotServer.getGuild();
        if (guild == null) return;

        switch (punishmentType) {
            case BAN:
                guild.unban(user).queue(success -> {}, error -> {});
                break;
            case MUTE:
                Member member = guild.getMemberById(user.getIdLong());
                if (member == null) break;

                guild.removeRoleFromMember(member, spotServer.getMuteRole()).queue(success -> {}, error -> {});
                break;
        }
    }

    public void expire() {
        this.time = null;

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE spot_punishments SET time = ? WHERE id = ?");
            ps.setString(1, this.time == null ? null : String.valueOf(this.time));
            ps.setLong(2, this.id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPending(boolean pending) {
        this.pending = pending;
        try {
            Connection con = manager.getConnection();

            if (!pending) {
                if (this.timeFor == null) this.time = null;
                else this.time = new Date().getTime() + (this.timeFor * 1000);
                PreparedStatement ps = con.prepareStatement("UPDATE spot_punishments SET time = ? WHERE id = ?");
                ps.setString(1, this.time == null ? null : String.valueOf(this.time));
                ps.setLong(2, this.id);
                ps.executeUpdate();
                this.updater();
            }
            PreparedStatement ps = con.prepareStatement("UPDATE spot_punishments SET pending = ? WHERE id = ?");
            ps.setBoolean(1, this.pending);
            ps.setLong(2, this.id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deletePunishment() {
        this.getUser(this::executeAction);
        this.manager.removePunishment(this);
        this.time = null;

        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM spot_punishments WHERE id = ?");
            ps.setLong(1, this.id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int compareTo(Punishment punishment) {
        return (int) (punishment.getID() - this.id);
    }
}
