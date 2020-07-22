package com.polydome.godemon.discordbot.view.table;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.*;

public class TableBuilder {
    private final Map<String, List<String>> data = new LinkedHashMap<>();
    private final String title;

    public TableBuilder(String title) {
        this.title = title;
    }

    public TableBuilder addColumns(String... columns) {
        for (final var column : columns) {
            data.put(column, new LinkedList<>());
        }

        return this;
    }

    public TableBuilder addRecord(Map<String, String> record) {
        for (final var entry : record.entrySet()) {
            try {
                data.get(entry.getKey()).add(entry.getValue());
            } catch (NullPointerException e) {
                throw new NoSuchColumnException(entry.getKey());
            }
        }

        return this;
    }

    private String joinToColumn(List<String> values) {
        StringBuilder builder = new StringBuilder();

        for (final var value : values) {
            builder.append(value).append("\n");
        }

        return builder.toString();
    }

    public MessageEmbed buildEmbed() {
        EmbedBuilder embed = new EmbedBuilder();

        if (title != null)
            embed.setTitle(title);

        for (final var column : data.keySet()) {
            embed.addField(column, joinToColumn(data.get(column)), true);
        }

        return embed.build();
    }
}
