package com.olziedev.spotbot.server.reaction;

public class ReactionRole {

    private final long roleID;
    private final String emoji;
    private final String label;
    private final String description;

    public ReactionRole(long roleID, String emoji, String label, String description) {
        this.roleID = roleID;
        this.emoji = emoji;
        this.label = label;
        this.description = description;
    }

    public long getRoleID() {
        return this.roleID;
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
}
