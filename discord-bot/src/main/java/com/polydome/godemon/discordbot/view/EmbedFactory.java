package com.polydome.godemon.discordbot.view;

import com.polydome.godemon.discordbot.view.service.GodData;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.discordbot.view.table.TableBuilder;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EmbedFactory {
    private final GodsDataProvider godsDataProvider;
    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm")
            .withZone(ZoneId.of("UTC"));
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("UTC"));
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withZone(ZoneId.of("UTC"));

    @Inject
    public EmbedFactory(GodsDataProvider godsDataProvider) {
        this.godsDataProvider = godsDataProvider;
    }

    private String createGodLabel(GodData godData, int usesLeft) {
        return String.format("`%dx` <%s> **%3$20s**", usesLeft, godData.getEmoteId(), godData.getName());
    }

    public MessageEmbed challengeStatus(int challengeId, ChallengeStatus status, boolean updatingFlag) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Challenge no. " + challengeId);

        builder.setDescription(
                String.format("Started %s at %s",
                        dateFormatter.format(status.getCreatedAt()),
                        timeFormatter.format(status.getCreatedAt())
                )
        );

        StringBuilder participants = new StringBuilder();
        for (final var participant : status.getParticipants()) {
            participants.append(participant).append("\n");
        }

        builder.addField("Participants", participants.toString(), true);
        builder.addField("Stats", String.format("Wins %d\nLoses %d\nKills %d\nDeaths %d", status.getWins(), status.getLoses(),  0, 0), true);

        builder.addBlankField(false);
        GodData godData;

        final int COLUMNS = 3;

        final StringBuilder[] columns = new StringBuilder[] {
                new StringBuilder(),
                new StringBuilder(),
                new StringBuilder()
        };

        int i = 0;
        for (final var entry : status.getGodToUsesLeft().entrySet()) {
            godData = godsDataProvider.findById(entry.getKey());
            columns[i % COLUMNS].append(createGodLabel(godData, entry.getValue())).append("\n");
            i++;
        }

        for (final var column : columns) {
            builder.addField("", column.toString(), true);
        }

        builder.addBlankField(false);
        builder.addField("","<:join:735488600980062210> Join", true);
        builder.addField("","<:history:735491281798823959> History", true);
        builder.addField("","<:sync:735567114638852178> Sync", true);

        if (updatingFlag)
            builder.setFooter("Updating...", "https://cdn.discordapp.com/emojis/735567114638852178");

        return builder.build();
    }

    public MessageEmbed challengesList(List<ChallengeBrief> challenges, String targetUserMention) {
        final var COLUMN_ID = "ID";
        final var COLUMN_QUEUE = "Queue";
        final var COLUMN_STARTED_AT = "Started at";

        TableBuilder table = new TableBuilder("Your Challenges");
        table.addColumns(COLUMN_ID, COLUMN_QUEUE, COLUMN_STARTED_AT);

        for (final var challenge : challenges) {
            table.addRecord(Map.ofEntries(
                    Map.entry(COLUMN_ID, String.valueOf(challenge.getId())),
                    Map.entry(COLUMN_QUEUE, String.valueOf(challenge.getGameMode())),
                    Map.entry(COLUMN_STARTED_AT, dateTimeFormatter.format(challenge.getCreatedAt()))
            ));
        }

        return table.buildEmbed();
    }

    public MessageEmbed lobby(List<ChallengeBrief> challenges) {
        final var COLUMN_ID = "ID";
        final var COLUMN_QUEUE = "Queue";
        final var COLUMN_STARTED_AT = "Started at";

        TableBuilder table = new TableBuilder(":crossed_swords: Lobby :crossed_swords:");
        table.addColumns(COLUMN_ID, COLUMN_QUEUE, COLUMN_STARTED_AT);

        for (final var challenge : challenges) {
            table.addRecord(Map.ofEntries(
                    Map.entry(COLUMN_ID, String.valueOf(challenge.getId())),
                    Map.entry(COLUMN_QUEUE, String.valueOf(challenge.getGameMode())),
                    Map.entry(COLUMN_STARTED_AT, dateTimeFormatter.format(challenge.getCreatedAt()))
            ));
        }

        return table.buildEmbed();
    }
}
