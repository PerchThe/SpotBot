package com.olziedev.spotbot.managers;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.server.punishment.Punishment;
import com.olziedev.spotbot.server.suggestion.Suggestion;
import com.olziedev.spotbot.server.ticket.Ticket;
import com.olziedev.spotbot.utils.SpotTimer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager extends Manager {

    public final Map<Long, Message> cachedMessages = new ConcurrentHashMap<>();
    public final Map<Long, List<Punishment>> cachedPunishments = new ConcurrentHashMap<>();
    public final Map<Long, List<Ticket>> cachedTickets = new ConcurrentHashMap<>();
    private final Map<Long, Suggestion> cachedSuggestions = new ConcurrentHashMap<>();

    private Connection connection;

    public DatabaseManager(JDA jda, SpotBot spotBot) {
        super(jda, spotBot);
    }

    @Override
    public void setup() {
        try {
            SpotBot.getLogger().info("Connecting to database...");
            File file = new File(SpotBot.getDataFolder() + File.separator + "data", "database.db");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            this.connect(file);
            Connection con = this.getConnection();
            con.prepareStatement("CREATE TABLE IF NOT EXISTS spot_suggestions(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " message_id LONG," +
                    " user_id LONG," +
                    " suggestion LONGTEXT)").execute();
            con.prepareStatement("CREATE TABLE IF NOT EXISTS spot_punishments(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " user_id LONGTEXT," +
                    " type LONGTEXT," +
                    " time LONG," +
                    " time_for INT," +
                    " time_on LONG," +
                    " reason LONGTEXT," +
                    " pending BOOLEAN," +
                    " executor LONG)").execute();
            con.prepareStatement("CREATE TABLE IF NOT EXISTS spot_tickets(id VARCHAR(255)," +
                    " owner_id LONGTEXT," +
                    " category LONGTEXT," +
                    " embed_message_id LONGTEXT," +
                    " PRIMARY KEY(id))").execute();
            try {
                con.prepareStatement("ALTER TABLE spot_suggestions ADD COLUMN members_voted LONGTEXT").execute();
            } catch (Exception ignored) {}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SpotTimer.schedule(s -> {
            this.cachedMessages.clear();
            System.gc();
            try {
                this.getConnection().close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 60 * 60 * 1000, 60 * 60 * 1000);
    }

    @Override
    public void load(Runnable runnable) {
        try {
            SpotBot.getLogger().info("Loading punishments...");
            Connection con = this.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM spot_punishments");
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                long punishmentID = result.getLong("id");
                this.addPunishment(new Punishment(punishmentID));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            SpotBot.getLogger().info("Loading tickets...");
            Connection con = this.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM spot_tickets");
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                long ticketID = result.getLong("id");
                this.addTicket(new Ticket(ticketID));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            SpotBot.getLogger().info("Loading suggestions...");
            Connection con = this.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM spot_suggestions");
            ResultSet result = ps.executeQuery();
            while (result.next()) {
                long suggestionID = result.getLong("id");
                this.addSuggestion(new Suggestion(suggestionID));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        runnable.run();
    }

    public void connect(File file) throws Exception {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
    }

    public Connection getConnection() throws Exception {
        if (this.connection.isClosed()) this.connect(new File(SpotBot.getDataFolder() + File.separator + "data", "database.db"));
        return this.connection;
    }

    public JDA getJDA() {
        return this.jda;
    }

    public List<Punishment> getPunishments(long user) {
        return this.cachedPunishments.getOrDefault(user, new ArrayList<>());
    }

    public void addPunishment(Punishment punishment) {
        List<Punishment> punishments = this.getPunishments(punishment.getUserID());
        punishments.add(punishment);
        Collections.sort(punishments);
        this.cachedPunishments.put(punishment.getUserID(), punishments);
    }

    public void removePunishment(Punishment punishment) {
        List<Punishment> punishments = this.getPunishments(punishment.getUserID());
        punishments.remove(punishment);
        Collections.sort(punishments);
        this.cachedPunishments.put(punishment.getUserID(), punishments);
    }

    public List<Ticket> getTickets(long user) {
        return this.cachedTickets.getOrDefault(user, new ArrayList<>());
    }

    public Ticket getTicket(TextChannel channel) {
        return this.cachedTickets.values().stream().flatMap(x -> x.stream()).filter(x -> x.getID() == channel.getIdLong()).findFirst().orElse(null);
    }

    public void addTicket(Ticket ticket) {
        if (ticket.getUserID() == -1) return;

        List<Ticket> tickets = this.getTickets(ticket.getUserID());
        tickets.add(ticket);
        this.cachedTickets.put(ticket.getUserID(), tickets);
    }

    public void removeTicket(Ticket ticket) {
        List<Ticket> tickets = this.getTickets(ticket.getUserID());
        tickets.remove(ticket);
        this.cachedTickets.put(ticket.getUserID(), tickets);
    }

    public void addSuggestion(Suggestion suggestion) {
        this.cachedSuggestions.put(suggestion.getID(), suggestion);
    }

    public void removeSuggestion(Suggestion suggestion) {
        this.cachedSuggestions.remove(suggestion.getID());
    }

    public Map<Long, Suggestion> getSuggestions() {
        return this.cachedSuggestions;
    }

    public Suggestion getSuggestion(long messageID) {
        return this.cachedSuggestions.values().stream().filter(x -> x.getMessageID() == messageID).findFirst().orElse(null);
    }
}
