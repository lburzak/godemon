package com.polydome.godemon.domain.service;

import com.polydome.godemon.domain.entity.Challenge;
import com.polydome.godemon.domain.entity.Challenger;
import com.polydome.godemon.domain.entity.GameMode;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ContributionRepository;
import com.polydome.godemon.domain.service.matchdetails.MatchDetails;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.service.matchdetails.PlayerRecord;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChallengeServiceTest {
    AutoCloseable closeable;

    final int CHALLENGE_ID = 10;

    ChallengeService SUT;
    @Mock MatchDetailsEndpoint matchDetailsEndpoint;
    @Mock ChallengeRepository challengeRepository;
    @Mock ContributionRepository contributionRepository;
    @Mock RandomNumberGenerator rng;

    @Captor ArgumentCaptor<Challenge> challengeCaptor;

    @BeforeAll
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setUpOne() {
        SUT = new ChallengeService(matchDetailsEndpoint, challengeRepository, contributionRepository, rng);
    }

    @AfterEach
    void tearDownOne() {
        clearInvocations(matchDetailsEndpoint, challengeRepository, contributionRepository);
    }

    @AfterAll
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void synchronizeChallengeTest() {
        when(challengeRepository.findChallenge(CHALLENGE_ID)).thenReturn(
                Challenge.builder()
                        .participants(List.of(createChallenger(12)))
                        .gameMode(GameMode.JOUST)
                        .availableGods(Map.of(30, 1))
                        .build()
        );

        when(matchDetailsEndpoint.fetchNewerMatches(eq(12), eq(GameMode.JOUST), any())).thenReturn(
                List.of(createMatch(
                        createPlayerRecord(12, 30, PlayerRecord.WinStatus.WINNER),
                        createPlayerRecord(0, 72, PlayerRecord.WinStatus.LOSER)
                ))
        );

        SUT.synchronizeChallenge(CHALLENGE_ID);

        verify(challengeRepository).updateChallenge(challengeCaptor.capture());

        assertThat(challengeCaptor.getValue().getAvailableGods().keySet(), hasItems(30, 72));
    }

    private Challenger createChallenger(int playerId) {
        return Challenger.builder()
                .inGameId(playerId)
                .build();
    }

    private MatchDetails createMatch(PlayerRecord... playerRecords) {
        return new MatchDetails(0, (byte) 2, playerRecords);
    }

    private PlayerRecord createPlayerRecord(int playerId, int godId, PlayerRecord.WinStatus winStatus) {
        return new PlayerRecord(
                playerId,
                "TestPlayer",
                (byte) 22,
                (byte) 13,
                godId,
                winStatus
        );
    }

}