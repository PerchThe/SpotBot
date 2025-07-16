package com.olziedev.spotbot.message;

import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpotMessage {

    private final SpotEmbed spotEmbed;
    private final boolean allowMentions;
    private List<Button> buttons;
    private final SlashExecutor slashExecutor;
    private final boolean ephemeral;

    private String content;

    public SpotMessage(FlatFileSection section, SlashExecutor slashExecutor) {
        this(section, slashExecutor, null);
    }

    public SpotMessage(FlatFileSection section, SlashExecutor slashExecutor, Consumer<EmbedBuilder> spotEmbed) {
        this.spotEmbed = section.get("embed") == null ? null : new SpotEmbed(section.getSection("embed"), slashExecutor, spotEmbed);
        this.buttons = new ArrayList<>();
        this.slashExecutor = slashExecutor;
        this.content = section.getString("content");
        this.allowMentions = section.getBoolean("allowMentions");
        this.ephemeral = section.getBoolean("ephemeral");
        this.buttons = getButtons(section);
    }

    public static List<Button> getButtons(FlatFileSection section) {
        List<Button> buttons = new ArrayList<>();
        FlatFileSection buttonsSection = section.getSection("buttons");
        for (String key : buttonsSection.singleLayerKeySet()) {
            FlatFileSection button = buttonsSection.getSection(key);

            buttons.add(Button.of(
                    button.getEnum("type", ButtonStyle.class),
                    button.getString("id"),
                    button.getString("name"),
                    button.getString("emoji").isEmpty()  ? null : Emoji.fromMarkdown(button.getString("emoji"))
            ));
        }
        return buttons;
    }

    public List<Button> getButtons() {
        return this.buttons;
    }

    public String getContent() {
        return content;
    }

    public SpotEmbed getSpotEmbed() {
        return spotEmbed;
    }

    public boolean hasEmbed() {
        return spotEmbed != null;
    }

    public ReplyAction sendMessage(Interaction channel) {
        ReplyAction action = (this.hasEmbed() ? channel.reply(this.content).addEmbeds(spotEmbed.build()) : channel.reply(this.content)).setEphemeral(this.ephemeral);
        if (this.buttons.isEmpty()) return action;

        SpotActionRow actionRow = new SpotActionRow();
        List<SpotActionRow> rows = new ArrayList<>(Collections.singletonList(actionRow));
        for (Button button : buttons) {
            if (!actionRow.add(button)) continue;

            actionRow = new SpotActionRow();
            rows.add(actionRow);
        }
        return action.addActionRows(rows.stream().map(x -> ActionRow.of(x.components)).collect(Collectors.toList()));
    }

    public WebhookMessageUpdateAction<Message> sendMessage(InteractionHook channel) {
        WebhookMessageUpdateAction<Message> action = this.hasEmbed() ? !this.content.isEmpty() ? channel.editOriginal(this.content).setEmbeds(spotEmbed.build()) : channel.editOriginalEmbeds(spotEmbed.build()) : channel.editOriginal(this.content);
        if (this.buttons.isEmpty()) return action;

        SpotActionRow actionRow = new SpotActionRow();
        List<SpotActionRow> rows = new ArrayList<>(Collections.singletonList(actionRow));
        for (Button button : buttons) {
            if (!actionRow.add(button)) continue;

            actionRow = new SpotActionRow();
            rows.add(actionRow);
        }
        return action.setActionRows(rows.stream().map(x -> ActionRow.of(x.components)).collect(Collectors.toList()));
    }

    public MessageAction sendMessage(MessageChannel channel) {
        MessageAction action = this.hasEmbed() ? !this.content.isEmpty() ? channel.sendMessageEmbeds(spotEmbed.build()).content(this.content) : channel.sendMessageEmbeds(spotEmbed.build()) : channel.sendMessage(this.content);
        if (this.allowMentions) action = action.allowedMentions(Arrays.asList(Message.MentionType.values()));
        if (this.buttons.isEmpty()) return action;

        SpotActionRow actionRow = new SpotActionRow();
        List<SpotActionRow> rows = new ArrayList<>(Collections.singletonList(actionRow));
        for (Button button : buttons) {
            if (!actionRow.add(button)) continue;

            actionRow = new SpotActionRow();
            rows.add(actionRow);
        }
        return action.setActionRows(rows.stream().map(x -> ActionRow.of(x.components)).collect(Collectors.toList()));
    }

    public MessageAction editMessage(Message message) {
        MessageAction action = this.hasEmbed() ? message.editMessageEmbeds(spotEmbed.build()).content(this.content) : message.editMessage(this.content);
        if (this.allowMentions) action = action.allowedMentions(Arrays.asList(Message.MentionType.values()));
        if (this.buttons.isEmpty()) return action;

        SpotActionRow actionRow = new SpotActionRow();
        List<SpotActionRow> rows = new ArrayList<>(Collections.singletonList(actionRow));
        for (Button button : buttons) {
            if (!actionRow.add(button)) continue;

            actionRow = new SpotActionRow();
            rows.add(actionRow);
        }
        return action.setActionRows(rows.stream().map(x -> ActionRow.of(x.components)).collect(Collectors.toList()));
    }

    public UpdateInteractionAction editMessage(ComponentInteraction message) {
        UpdateInteractionAction action = this.hasEmbed() ? message.editMessageEmbeds(spotEmbed.build()).setContent(this.content) : message.editMessage(this.content);
        if (this.buttons.isEmpty()) return action;

        SpotActionRow actionRow = new SpotActionRow();
        List<SpotActionRow> rows = new ArrayList<>(Collections.singletonList(actionRow));
        for (Button button : buttons) {
            if (!actionRow.add(button)) continue;

            actionRow = new SpotActionRow();
            rows.add(actionRow);
        }
        return action.setActionRows(rows.stream().map(x -> ActionRow.of(x.components)).collect(Collectors.toList()));
    }

    public WebhookMessageUpdateAction<Message> editMessage(InteractionHook message) {
        WebhookMessageUpdateAction<Message> action = this.hasEmbed() ? message.editOriginalEmbeds(spotEmbed.build()).setContent(this.content) : message.editOriginal(this.content);
        if (this.buttons.isEmpty()) return action;

        SpotActionRow actionRow = new SpotActionRow();
        List<SpotActionRow> rows = new ArrayList<>(Collections.singletonList(actionRow));
        for (Button button : buttons) {
            if (!actionRow.add(button)) continue;

            actionRow = new SpotActionRow();
            rows.add(actionRow);
        }
        return action.setActionRows(rows.stream().map(x -> ActionRow.of(x.components)).collect(Collectors.toList()));
    }

    public RestAction<?> sendMessage() {
        return this.slashExecutor.getEvent() == null ? this.sendMessage(this.slashExecutor.getChannel()) : this.sendMessage(this.slashExecutor.getEvent());
    }

    public <T extends RestAction<?>> T sendMessage(Class<? extends T> clazz) {
        return this.slashExecutor.getEvent() == null ? clazz.cast(this.sendMessage(this.slashExecutor.getChannel())) : clazz.cast(this.sendMessage(this.slashExecutor.getEvent()));
    }

    public SpotMessage replaceOptions(List<OptionData> dataList) {
        for (OptionData data : dataList) {
            OptionMapping option = this.slashExecutor.getOption(data.getName());
            this.replaceOptions("%option_" + data.getName().toLowerCase() + "%", option == null ? "N/A" : option.getAsString());
        }
        return this;
    }

    public SpotMessage replaceOptions(String placeholder, String value) {
        this.content = this.content.replace(placeholder, value);
        if (this.buttons.isEmpty()) return this;

        this.buttons = this.buttons.stream()
                .map(x -> {
                    if (x.getUrl() == null) return x;

                    return x.withUrl(x.getUrl().replace(placeholder, value));
                })
                .map(x -> x.withLabel(x.getLabel().replace(placeholder, value)))
                .collect(Collectors.toList());
        return this;
    }

    public Button getButton(String id) {
        return this.buttons.stream().filter(x -> x.getId() != null && x.getId().equals(id)).findFirst().orElse(null);
    }

    private static class SpotActionRow {

        private final List<Component> components;

        public SpotActionRow() {
            this.components = new ArrayList<>();
        }

        public boolean add(Component component) {
            this.components.add(component);
            return components.size() == 5;
        }
    }
}
