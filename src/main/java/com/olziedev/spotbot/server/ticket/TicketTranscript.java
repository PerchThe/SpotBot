package com.olziedev.spotbot.server.ticket;

import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.utils.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.function.Consumer;

public class TicketTranscript {
    private final Ticket ticket;
    private final long id;
    private final String path;

    public TicketTranscript(Ticket ticket) {
        this.ticket = ticket;
        Random random = new Random();
        this.id = (long) random.nextInt(Integer.MAX_VALUE) * random.nextInt(Integer.MAX_VALUE) + 10000000L;
        this.path = Configuration.getConfig().getString("settings.ticket.url") + "/" + ticket.getUserID() + "/" + id + ".html";
    }

    public void createTranscript(Consumer<String> callback) {
        try {
            String fileName = this.id + ".html";
            File file = new File(SpotBot.getDataFolder(), "transcripts");
            try {
                Process process = Runtime.getRuntime().exec("/usr/bin/dotnet DiscordChatExporter.Cli.dll export --token " + SpotBot.getJDA().getToken().substring(4) + " --channel " + this.ticket.getID() + " --output " + fileName, null, file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    SpotBot.getLogger().info("Transcript log: " + line);
                    if (line.contains("Exported failed")) {
                        if (callback != null) callback.accept(null);
                        process.destroy();
                        return;
                    }
                    if (!line.contains("Successfully exported")) continue;
                    process.destroy();
                    File localFile = new File(Configuration.getConfig().getString("settings.ticket.local-html") + "/" + ticket.getUserID() + "/" + fileName);
                    if (!localFile.getParentFile().exists()) {
                        localFile.getParentFile().mkdirs();
                    }
                    System.out.println(new File(file, fileName).renameTo(localFile));

                    if (callback != null) callback.accept(this.path);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
