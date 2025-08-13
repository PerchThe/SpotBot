package com.olziedev.spotbot.server.ticket;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import com.olziedev.olziecommand.framework.api.FrameworkCommand;

import java.util.List;
import java.util.stream.Collectors;

public class TicketCategory {

    private final String key;
    private final long categoryID;
    private final List<Long> roleIDs;
    private final List<Long> viewIDs;
    private final String emoji;
    private final String label;
    private final String description;
    private final List<String> questions;
    private final String name;

    public TicketCategory(String key, long categoryID, List<String> roleIDs, List<String> viewIDs, String emoji, String label, String description, List<String> questions, String name) {
        this.key = key;
        this.categoryID = categoryID;
        this.roleIDs = roleIDs.stream().map(Long::parseLong).collect(Collectors.toList());
        this.viewIDs = viewIDs.stream().map(Long::parseLong).collect(Collectors.toList());
        this.emoji = emoji;
        this.label = label;
        this.description = description;
        this.questions = questions;
        this.name = name;
    }

    public String getKey() {
        return this.key;
    }

    public long getCategoryID() {
        return this.categoryID;
    }

    public List<Long> getRoleIDs() {
        return this.roleIDs;
    }

    public List<Long> getViewIDs() {
        return this.viewIDs;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getQuestions() {
        return this.questions;
    }

    public String getName() {
        return this.name;
    }

    public boolean noPermission(com.olziedev.olziecommand.framework.api.FrameworkCommand cmd, Member member, net.dv8tion.jda.api.entities.User user) {
        return this.viewIDs.stream().noneMatch(x -> member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(x2 -> x2.equals(x)));
    }

    public boolean noPermission(Member member) {
        return this.viewIDs.stream().noneMatch(x -> member.getRoles().stream().map(ISnowflake::getIdLong).anyMatch(x2 -> x2.equals(x)));
    }
}
