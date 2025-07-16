package com.olziedev.spotbot.message;

import com.olziedev.olziecommand.framework.executor.SlashExecutor;
import de.leonhard.storage.sections.FlatFileSection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public class SpotEmbed {

    private final EmbedBuilder embedBuilder;
    private final SlashExecutor slashExecutor;

    public SpotEmbed(FlatFileSection section, SlashExecutor slashExecutor, Consumer<EmbedBuilder> spotEmbed) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title = section.getString("title");
        if (spotEmbed != null) spotEmbed.accept(embedBuilder);
        if (!title.isEmpty()) embedBuilder.setTitle(title);

        embedBuilder.setDescription(section.getString("description"));
        if (section.getBoolean("timestamp")) embedBuilder.setTimestamp(Instant.now());

        String color = section.getString("color");
        if (!color.isEmpty()) embedBuilder.setColor(Color.decode(color));

        String image = section.getString("image");
        if (!image.isEmpty()) embedBuilder.setImage(image);

        String thumbnail = section.getString("thumbnail");
        if (!thumbnail.isEmpty()) embedBuilder.setThumbnail(thumbnail);

        FlatFileSection footer = section.get("footer") == null ? null : section.getSection("footer");
        if (footer != null) {
            embedBuilder.setFooter(footer.getString("text"), footer.getString("icon_url").isEmpty() ? null : footer.getString("icon_url"));
        }
        FlatFileSection author = section.get("author") == null ? null : section.getSection("author");
        if (author != null) {
            embedBuilder.setAuthor(author.getString("name"), author.getString("url").isEmpty() ? null : author.getString("url"), author.getString("icon_url").isEmpty() ? null : author.getString("icon_url"));
        }

        FlatFileSection field = section.get("fields") == null ? null : section.getSection("fields");
        if (field != null) {
            for (String key : field.singleLayerKeySet()) {
                embedBuilder.addField(field.getString(key + ".name"), field.getString(key + ".value"), field.getBoolean(key + ".inline"));
            }
        }
        this.embedBuilder = embedBuilder;
        this.slashExecutor = slashExecutor;
    }

    public void replaceOptions(List<OptionData> dataList) {
        for (OptionData data : dataList) {
            String placeholder = "%option_" + data.getName().toLowerCase() + "%";
            OptionMapping option = this.slashExecutor.getOption(data.getName());
            this.replaceOptions(placeholder, option == null ? "N/A" : option.getAsString());
        }
    }

    public void replaceOptions(String placeholder, String value) {
        MessageEmbed oldEmbed = this.embedBuilder.build();
        this.embedBuilder.setTitle(oldEmbed.getTitle() == null ? null : oldEmbed.getTitle().replace(placeholder, value));
        this.embedBuilder.setDescription(oldEmbed.getDescription() == null ? null : oldEmbed.getDescription().replace(placeholder, value));
        this.embedBuilder.setFooter(oldEmbed.getFooter() == null ? null : oldEmbed.getFooter().getText().replace(placeholder, value), oldEmbed.getFooter() == null ? null : oldEmbed.getFooter().getIconUrl());
        this.embedBuilder.setAuthor(oldEmbed.getAuthor() == null ? null : oldEmbed.getAuthor().getName().replace(placeholder, value), oldEmbed.getAuthor() == null ? null : oldEmbed.getAuthor().getUrl(), oldEmbed.getAuthor() == null ? null : oldEmbed.getAuthor().getIconUrl());

        List<MessageEmbed.Field> fields = this.embedBuilder.getFields();
        for (int i = 0; i < fields.size(); i++) {
            MessageEmbed.Field field = fields.get(i);
            fields.set(i, new MessageEmbed.Field(
                    field.getName().replace(placeholder, value),
                    field.getValue().replace(placeholder, value),
                    field.isInline())
            );
        }
    }

    public EmbedBuilder getEmbedBuilder() {
        return this.embedBuilder;
    }

    public MessageEmbed build() {
        return this.embedBuilder.build();
    }
}
