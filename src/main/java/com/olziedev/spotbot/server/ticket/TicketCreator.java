package com.olziedev.spotbot.server.ticket;

import com.olziedev.olziecomponent.OlzieComponent;
import com.olziedev.spotbot.SpotBot;
import com.olziedev.spotbot.managers.DatabaseManager;
import com.olziedev.spotbot.message.SpotMessage;
import com.olziedev.spotbot.utils.Configuration;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TicketCreator {

    private final static JDA jda = SpotBot.getJDA();

    private final long channelID;
    private final SpotMessage spotMessage;
    private final List<TicketCategory> categories;
    private final List<Permission> permissions;
    private String messageID;
    private final SelectionMenu selectionMenu;

    public TicketCreator(FlatFileSection section) {
        this.channelID = section.getLong("channel-id");
        this.messageID = Configuration.getStaticData().getString("ticket.message-id");
        this.spotMessage = new SpotMessage(section.getSection("message"), null);
        this.categories = new ArrayList<>();

        for (String key : section.getSection("categories").singleLayerKeySet()) {
            FlatFileSection category = section.getSection("categories." + key);
            this.categories.add(new TicketCategory(key, category.getLong("category-id"), category.getStringList("pinged-roles"), category.getStringList("view-roles"), category.getString("emoji"), category.getString("label"), category.getString("description"), category.getStringList("questions"), category.getString("channel-name")));
        }
        SelectionMenu.Builder menu = SelectionMenu.create("spotbot_ticket_category");
        for (TicketCategory category : this.categories) {
            menu.addOption(category.getLabel(), category.getLabel(), category.getDescription(), Emoji.fromMarkdown(category.getEmoji()));
        }
        menu.setRequiredRange(1, 1);
        menu.setPlaceholder(section.getString("dropdown-placeholder"));

        this.permissions = section.getStringList("permissions").stream().map(Permission::valueOf).collect(Collectors.toList());

        OlzieComponent olzieComponent = SpotBot.getOlzieComponent();
        DatabaseManager manager = SpotBot.getDatabaseManager();
        this.selectionMenu = olzieComponent.addSelectionMenu(menu.build(), (t, event) -> {
            Member member = event.getMember();
            TicketCategory category = this.categories.stream().filter(x -> x.getLabel().equals(event.getSelectedOptions().get(0).getLabel())).findFirst().orElse(null);
            if (category == null) return null;

            Ticket ticket = manager.getTickets(member.getIdLong()).stream().filter(x -> x.getCategory().equals(category)).findFirst().orElse(null);
            event.getMessage().editMessage(event.getMessage()).setActionRows(ActionRow.of(event.getSelectionMenu())).queue();
            if (ticket != null) {
                return Configuration.getLang().getString("lang.ticket-open").replace("%channel%", ticket.getTextChannel().getAsMention());
            }

            ChannelAction<TextChannel> channelCreate = jda.getCategoryById(category.getCategoryID())
                    .createTextChannel(category.getName().replace("%user%", member.getUser().getName()).replace("%tag%", member.getUser().getAsTag()))
                    .syncPermissionOverrides()
                    .addMemberPermissionOverride(member.getIdLong(), permissions, new ArrayList<>());
            for (long roleID : category.getViewIDs()) channelCreate = channelCreate.addRolePermissionOverride(roleID, permissions, new ArrayList<>());

            TextChannel channel = channelCreate.complete();
            SpotBot.getExecutor().submit(() -> {
                this.createTicket(manager, category, channel, member);

                SpotMessage ticketMessage = new SpotMessage(Configuration.getConfig().getSection("settings.ticket.categories." + category.getKey() + ".ticket-created-message"), null);
                if (ticketMessage.hasEmbed()) {
                    ticketMessage.getSpotEmbed().replaceOptions("%member%", member.getAsMention());
                    ticketMessage.getSpotEmbed().replaceOptions("%category%", category.getLabel());
                }
                ticketMessage.sendMessage(channel).queue(m -> this.sendQuestionMessage(channel, member, category.getQuestions(), 0, new ArrayList<>(), answers -> {
                    channel.getHistory().retrievePast(100).queue(messages -> channel.deleteMessages(messages).queue(done -> {
                        StringBuilder roles = new StringBuilder();
                        category.getRoleIDs()
                                .stream()
                                .map(jda::getRoleById)
                                .filter(Objects::nonNull)
                                .forEach(x -> roles.append(x.getAsMention()).append(" "));
                        SpotMessage answersMessage = new SpotMessage(Configuration.getConfig().getSection("settings.ticket.categories." + category.getKey() + ".ticket-answers-message"), null);
                        StringBuilder questionsAnswers = new StringBuilder();
                        String answerEntry = Configuration.getConfig().getString("settings.ticket.categories." + category.getKey() + ".ticket-answers-entry");
                        for (int i = 0; i < category.getQuestions().size(); i++) {
                            String answer = answers.get(i).getContentRaw();
                            String question = category.getQuestions().get(i);
                            questionsAnswers.append(answerEntry.replace("%question%", question).replace("%answer%", answer));
                        }
                        answersMessage.replaceOptions("%ping%", roles.toString());
                        if (answersMessage.hasEmbed()) {
                            answersMessage.getSpotEmbed().replaceOptions("%member%", member.getAsMention());
                            answersMessage.getSpotEmbed().replaceOptions("%category%", category.getLabel());
                            answersMessage.getSpotEmbed().replaceOptions("%questions_answers%", questionsAnswers.toString());
                        }
                        MessageAction action = answersMessage.sendMessage(channel).allowedMentions(Arrays.asList(Message.MentionType.values()));
                        for (Message answer : answers) {
                            for (Message.Attachment attachment : answer.getAttachments()) {
                                try {
                                    action = action.addFile(attachment.retrieveInputStream().get(), attachment.getFileName());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        action.queue();
                    }));
                }));
            });
            return section.getString("reply-message").replace("%channel%", channel.getAsMention());
        }, true, (Member) null).getComponent();
    }

    private void sendQuestionMessage(TextChannel channel, Member member, List<String> questions, int position, List<Message> answers, Consumer<List<Message>> consumer) {
        if (position >= questions.size()) {
            consumer.accept(answers);
            return;
        }
        SpotMessage spotMessage = new SpotMessage(Configuration.getConfig().getSection("settings.ticket.question"), null);
        spotMessage.replaceOptions("%question%", questions.get(position));
        if (spotMessage.hasEmbed()) {
            spotMessage.getSpotEmbed().replaceOptions("%question%", questions.get(position));
        }
        spotMessage.sendMessage(channel).queue();
        SpotBot.getEventWaiter().waitEvent(GuildMessageReceivedEvent.class, condition ->
                condition.getChannel().getIdLong() == channel.getIdLong() && condition.getAuthor().getIdLong() == member.getIdLong(), event -> {
            String msg = event.getMessage().getContentRaw();
            if (msg.equalsIgnoreCase("cancel")) {
                SpotBot.getDatabaseManager().getTicket(channel).deleteTicket();
                channel.delete().queue();
                return;
            }
            answers.add(event.getMessage());
            this.sendQuestionMessage(channel, member, questions, position + 1, answers, consumer);
        }, 10, TimeUnit.MINUTES, () -> {
            SpotBot.getDatabaseManager().getTicket(channel).deleteTicket();
            channel.delete().queue();
        });
    }

    private void createTicket(DatabaseManager manager, TicketCategory category, TextChannel channel, Member member) {
        try {
            Connection con = manager.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO spot_tickets(id, owner_id, category) VALUES(?, ?, ?)");
            ps.setLong(1, channel.getIdLong());
            ps.setLong(2, member.getIdLong());
            ps.setString(3, category.getKey());
//            ps.setLong(4, x.getIdLong());
            ps.executeUpdate();
            Ticket ticket = new Ticket(channel.getIdLong());
            manager.addTicket(ticket);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public List<TicketCategory> getCategories() {
        return this.categories;
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public SelectionMenu getSelectionMenu() {
        return this.selectionMenu;
    }
}
