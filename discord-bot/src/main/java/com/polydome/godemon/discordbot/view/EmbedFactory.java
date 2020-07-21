package com.polydome.godemon.discordbot.view;

import com.polydome.godemon.discordbot.view.service.GodData;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.domain.model.ChallengeBrief;
import com.polydome.godemon.domain.model.ChallengeStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmbedFactory {
    private final GodsDataProvider godsDataProvider;
    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withZone(ZoneId.of("UTC"));

    @Inject
    public EmbedFactory(GodsDataProvider godsDataProvider) {
        this.godsDataProvider = godsDataProvider;
    }

    private String createGodLabel(GodData godData, int usesLeft) {
        return String.format("`%dx` <%s> **%3$20s**", usesLeft, godData.getEmoteId(), godData.getName());
    }

    public MessageEmbed challengeStatus(ChallengeStatus status) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Challenge");

        StringBuilder participants = new StringBuilder();
        for (final var participant : status.getParticipants()) {
            participants.append(participant).append("\n");
        }

        builder.addField("Participants", participants.toString(), true);
        builder.addField("Stats", String.format("Wins %d\nLoses %d\nKills %d\nDeaths %d", status.getWins(), status.getLoses(), 11, 37), true);

        builder.addBlankField(false);
        GodData godData;

        final int COLUMNS = 3;
        final int rows = status.getGodsLeftCount() / COLUMNS;

        int row = 1;
        StringBuilder godsColumn = new StringBuilder();
        for (var entry : status.getGodToUsesLeft().entrySet()) {
            godData = godsDataProvider.findById(entry.getKey());
            godsColumn.append(createGodLabel(godData, entry.getValue())).append("\n");

            if (row == rows) {
                builder.addField("", godsColumn.toString(), true);
                godsColumn = new StringBuilder();
                row = 1;
            }

            row++;
        }

        return builder.build();
    }

    public MessageEmbed challengesList(List<ChallengeBrief> challenges, String targetUserMention) {
        StringBuilder idColumn = new StringBuilder();
        StringBuilder gameModeColumn = new StringBuilder();
        StringBuilder dateColumn = new StringBuilder();

        for (final var challenge : challenges) {
            idColumn.append(challenge.getId()).append("\n");
            gameModeColumn.append(challenge.getGameMode()).append("\n");
            dateColumn.append(dateTimeFormatter.format(challenge.getLastUpdate())).append("\n");
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        return embedBuilder
                .setTitle(String.format("%s's challenges", targetUserMention))
                .addField("ID", idColumn.toString(), true)
                .addField("Queue", gameModeColumn.toString(), true)
                .addField("Last update", dateColumn.toString(), true)
                .build();
    }
}
