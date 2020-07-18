package com.polydome.godemon.discordbotapp.di;

import com.polydome.godemon.data.dao.ChallengeDAO;
import com.polydome.godemon.data.dao.ChallengerDAO;
import com.polydome.godemon.data.dao.GodDAO;
import com.polydome.godemon.data.dao.PropositionDAO;
import com.polydome.godemon.discordbot.listener.CommandListener;
import com.polydome.godemon.discordbot.listener.ReactionListener;
import com.polydome.godemon.domain.repository.ChallengeRepository;
import com.polydome.godemon.domain.repository.ChallengerRepository;
import com.polydome.godemon.domain.repository.ChampionRepository;
import com.polydome.godemon.domain.repository.PropositionRepository;
import com.polydome.godemon.domain.service.ChallengeService;
import com.polydome.godemon.domain.service.GameRulesProvider;
import com.polydome.godemon.domain.service.PlayerEndpoint;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.usecase.*;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import com.polydome.godemon.presentation.controller.ChallengeController;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smiteapi.json.InstantAdapter;
import com.polydome.godemon.smiteapi.json.QueueAdapter;
import com.polydome.godemon.smitedata.implementation.*;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import com.polydome.godemon.smitedata.repository.SmiteChampionRepository;
import com.squareup.moshi.Moshi;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.inject.Singleton;
import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@PropertySource("file:application.properties")
@ComponentScan("com.polydome.godemon.discordbot")
@ComponentScan( "com.polydome.godemon.discordbotapp")
public class Config {
    // Discord

    @Bean
    @Singleton
    public JDA jda(
            final @Value("${discord.botToken}") String botToken,
            final CommandListener commandListener,
            final ReactionListener reactionListener
    ) throws LoginException {
        return JDABuilder
                .createLight(botToken, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(commandListener, reactionListener)
                .build();
    }

    // Presentation

    @Bean
    public ChallengeContract.Presenter challengeContractPresenter(
            final AcceptChallengeUseCase acceptChallengeUseCase,
            final GetAvailableChallengesUseCase getAvailableChallengesUseCase,
            final GetChallengeStatusUseCase getChallengeStatusUseCase,
            final IntroduceUseCase introduceUseCase,
            final JoinChallengeUseCase joinChallengeUseCase,
            final StartChallengeUseCase startChallengeUseCase
    ) {
        return new ChallengeController(joinChallengeUseCase, acceptChallengeUseCase, getChallengeStatusUseCase, introduceUseCase,
                startChallengeUseCase, getAvailableChallengesUseCase);
    }

    // Domain

    @Bean
    public AcceptChallengeUseCase acceptChallengeUseCase(
            ChallengerRepository challengerRepository,
            ChallengeRepository challengeRepository,
            PropositionRepository propositionRepository
    ) {
        return new AcceptChallengeUseCase(challengerRepository, challengeRepository, propositionRepository);
    }

    @Bean
    public GetAvailableChallengesUseCase getAvailableChallengesUseCase(
            ChallengeRepository challengeRepository
    ) {
        return new GetAvailableChallengesUseCase(challengeRepository);
    }

    @Bean
    public GetChallengeStatusUseCase getChallengeStatusUseCase(
            ChallengerRepository challengerRepository,
            ChallengeRepository challengeRepository,
            ChallengeService challengeService
    ) {
        return new GetChallengeStatusUseCase(challengerRepository, challengeRepository, challengeService);
    }

    @Bean
    public IntroduceUseCase introduceUseCase(
            ChallengerRepository challengerRepository,
            PlayerEndpoint playerEndpoint
    ) {
        return new IntroduceUseCase(challengerRepository, playerEndpoint);
    }

    @Bean
    public JoinChallengeUseCase joinChallengeUseCase(
            ChallengeService challengeService,
            ChallengeRepository challengeRepository,
            ChallengerRepository challengerRepository,
            ChampionRepository championRepository,
            GameRulesProvider gameRulesProvider,
            PropositionRepository propositionRepository
    ) {
        return new JoinChallengeUseCase(challengeService, challengeRepository, challengerRepository, championRepository, gameRulesProvider, propositionRepository);
    }

    @Bean
    public StartChallengeUseCase startChallengeUseCase(
            ChallengerRepository challengerRepository,
            ChallengeRepository challengeRepository
    ) {
        return new StartChallengeUseCase(challengerRepository, challengeRepository);
    }

    @Bean
    public ChallengeService challengeService(MatchDetailsEndpoint matchDetailsEndpoint, ChallengeRepository challengeRepository) {
        return new ChallengeService(matchDetailsEndpoint, challengeRepository);
    }

    // Smite data

    @Bean
    public ChampionRepository championRepository(SmiteChampionRepository smiteChampionRepository) {
        return new SmiteGodsDataProvider(smiteChampionRepository);
    }

    @Bean
    GameRulesProvider gameRulesProvider(GodsRepository godsRepository) {
        return new SmiteRulesProvider(godsRepository);
    }

    // Data

    @Bean
    public ChallengerRepository challengerRepository(Connection connection) throws SQLException {
        return new ChallengerDAO(connection);
    }

    @Bean
    public ChallengeRepository challengeRepository(Connection connection, SmiteGameModeService gameModeService) throws SQLException {
        return new ChallengeDAO(connection, gameModeService);
    }

    @Bean
    public PropositionRepository propositionRepository(Connection connection) throws SQLException {
        return new PropositionDAO(connection);
    }

    @Bean
    public GodsRepository godsRepository(Connection connection) throws SQLException {
        return new GodDAO(connection);
    }

    @Bean
    public SmiteChampionRepository smiteChampionRepository(Connection connection) throws SQLException {
        return new GodDAO(connection);
    }

    @Bean
    @Singleton
    public Connection connection(
            final @Value("${db.url}") String url,
            final @Value("${db.username}") String username,
            final @Value("${db.password}") String password
    ) throws SQLException {
        return DriverManager
                .getConnection(
                        String.format("jdbc:%s?useLegacyDatetimeCode=false&serverTimezone=UTC", url),
                        username,
                        password);
    }

    @Bean
    public SmiteGameModeService smiteGameModeService() {
        return new SmiteGameModeService();
    }

    // Endpoint

    @Bean
    public PlayerEndpoint playerEndpoint(SmiteApiClient smiteApiClient) {
        return new SmitePlayerEndpoint(smiteApiClient);
    }

    @Bean
    public MatchDetailsEndpoint matchDetailsEndpoint(SmiteApiClient smiteApiClient) {
        return new SmiteMatchDetailsEndpoint(smiteApiClient);
    }

    @Bean
    @Singleton
    public SmiteApiClient smiteApiClient(
            final @Value("${hirez.devId}") String devId,
            final @Value("${hirez.authKey}") String authKey,
            final OkHttpClient httpClient,
            final Moshi moshi
    ) {
        return SmiteApiClient.builder()
                .endpointUrl("http://api.smitegame.com/smiteapi.svc")
                .devId(devId)
                .authKey(authKey)
                .httpClient(httpClient)
                .moshi(moshi)
                .build();
    }

    @Bean
    @Singleton
    public Moshi moshi(InstantAdapter instantAdapter, QueueAdapter queueAdapter) {
        return new Moshi.Builder()
                .add(instantAdapter)
                .add(queueAdapter)
                .build();
    }

    @Bean
    @Singleton
    public InstantAdapter instantAdapter() {
        return new InstantAdapter();
    }

    @Bean
    @Singleton
    public QueueAdapter queueAdapter() {
        return new QueueAdapter();
    }

    @Bean
    @Singleton
    public OkHttpClient okHttpClient() {
        return new OkHttpClient().newBuilder().build();
    }
}