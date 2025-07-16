package com.olziedev.spotbot.server.reaction;

import com.olziedev.olziecomponent.OlzieComponent;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpotReaction {

    private final static JDA jda = SpotBot.getJDA();

    private final long channelID;
    private final SpotMessage spotMessage;
    private final List<ReactionRole> roles;
    private String messageID;
    private final SelectionMenu selectionMenu;

    public SpotReaction(String name, FlatFileSection section) {
        this.channelID = section.getLong("channel-id");
        this.messageID = Configuration.getStaticData().getString("data.reactions." + name + ".message-id");
        this.spotMessage = new SpotMessage(section.getSection("message"), null);
        this.roles = new ArrayList<>();


        for (String key : section.getSection("roles").singleLayerKeySet()) {
            FlatFileSection role = section.getSection("roles." + key);
            this.roles.add(new ReactionRole(role.getLong("role-id"), role.getString("emoji"), role.getString("label"), role.getString("description")));
        }
        SelectionMenu.Builder menu = SelectionMenu.create("spotbot_reactions_" + name);
        for (ReactionRole role : this.roles) {
            menu.addOption(role.getLabel(), role.getLabel(), role.getDescription(), Emoji.fromMarkdown(role.getEmoji()));
        }
        menu.setRequiredRange(0, 25);
        menu.setPlaceholder(section.getString("dropdown-placeholder"));

        int limit = section.getOrDefault("limit", -1);
        OlzieComponent olzieComponent = SpotBot.getOlzieComponent();
        this.selectionMenu = olzieComponent.addSelectionMenu(menu.build(), (t, event) -> {
            for (SelectOption options : event.getSelectedOptions()) {
                ReactionRole reactionRole = this.roles.stream().filter(x -> x.getLabel().equals(options.getLabel())).findFirst().orElse(null);
                if (reactionRole == null) continue;

                Member member = event.getMember();
                Role role = jda.getRoleById(reactionRole.getRoleID());
                if (member == null || role == null) continue;

                if (member.getRoles().contains(role)) {
                    member.getGuild().removeRoleFromMember(member, role).queue();
                    continue;
                }
                if (limit != -1 && member.getRoles().stream().filter(x -> this.roles.stream().anyMatch(x2 -> x2.getRoleID() == x.getIdLong())).count() >= limit) {
                    return section.getString("reply-message-limit");
                }
                member.getGuild().addRoleToMember(member, role).queue();
            }
            event.getMessage().editMessage(event.getMessage()).setActionRows(ActionRow.of(event.getSelectionMenu())).queue();
            return section.getString("reply-message");
        }, true, (Member) null).getComponent();
    }

    public SpotMessage getSpotMessage() {
        return this.spotMessage;
    }

    public TextChannel getChannel() {
        return jda.getTextChannelById(this.channelID);
    }

    public void getMessage(Consumer<Message> callback) {
        if (this.messageID.isEmpty()) {
            callback.accept(null);
            return;
        }
        this.getChannel().retrieveMessageById(this.messageID).queue(callback, error -> callback.accept(null));
    }

    public List<ReactionRole> getRoles() {
        return this.roles;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public SelectionMenu getSelectionMenu() {
        return this.selectionMenu;
    }
}
