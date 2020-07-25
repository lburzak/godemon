package com.polydome.godemon.discordbotapp.di;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.polydome.godemon.data.dao.*;
import com.polydome.godemon.data.redis.RedisStorage;
import com.polydome.godemon.discordbot.listener.CommandListener;
import com.polydome.godemon.discordbot.listener.MessageActionListener;
import com.polydome.godemon.discordbot.reaction.ReactionActionBus;
import com.polydome.godemon.discordbot.view.service.GodsDataProvider;
import com.polydome.godemon.domain.repository.*;
import com.polydome.godemon.domain.service.ChallengeService;
import com.polydome.godemon.domain.service.GameRulesProvider;
import com.polydome.godemon.domain.service.PlayerEndpoint;
import com.polydome.godemon.domain.service.RandomNumberGenerator;
import com.polydome.godemon.domain.service.matchdetails.MatchDetailsEndpoint;
import com.polydome.godemon.domain.usecase.*;
import com.polydome.godemon.presentation.contract.ChallengeContract;
import com.polydome.godemon.presentation.controller.ChallengeController;
import com.polydome.godemon.smiteapi.client.SessionStorage;
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
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.Jedis;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@PropertySource("file:application.properties")
@ComponentScan("com.polydome.godemon.discordbot")
@ComponentScan( "com.polydome.godemon.discordbotapp")
public class Config {
    @Bean
    public Logger logger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
    }

    // Discord

    @Bean
    @Singleton
    public JDA jda(
            final @Value("${discord.botToken}") String botToken,
            final CommandListener commandListener,
            final ReactionActionBus reactionActionBus,
            final MessageActionListener messageActionListener
    ) throws LoginException {
        reactionActionBus.setListener(messageActionListener);
        return JDABuilder
                .createLight(botToken, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS)
                .enableCache(CacheFlag.EMOTE)
                .addEventListeners(commandListener, reactionActionBus)
                .build();
    }

    // Presentation

    @Bean
    public @Named("CommandPrefix") String commandPrefix(final @Value("${discord.prefix}") String prefix) {
        return prefix;
    }

    @Bean
    public ChallengeContract.Presenter challengeContractPresenter(
            final AcceptChallengeUseCase acceptChallengeUseCase,
            final GetAvailableChallengesUseCase getAvailableChallengesUseCase,
            final GetChallengeStatusUseCase getChallengeStatusUseCase,
            final IntroduceUseCase introduceUseCase,
            final JoinChallengeUseCase joinChallengeUseCase,
            final StartChallengeUseCase startChallengeUseCase,
            final GetAllChallengesUseCase getAllChallengesUseCase
    ) {
        return new ChallengeController(joinChallengeUseCase, acceptChallengeUseCase, getChallengeStatusUseCase, introduceUseCase,
                startChallengeUseCase, getAvailableChallengesUseCase, getAllChallengesUseCase);
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
            ChallengeService challengeService,
            ContributionRepository contributionRepository
    ) {
        return new GetChallengeStatusUseCase(challengerRepository, challengeRepository, challengeService, contributionRepository);
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
    public GetAllChallengesUseCase getAllChallengesUseCase(
            ChallengerRepository challengerRepository,
            ChallengeRepository challengeRepository
    ) {
        return new GetAllChallengesUseCase(challengerRepository, challengeRepository);
    }

    @Bean
    public ChallengeService challengeService(MatchDetailsEndpoint matchDetailsEndpoint, ChallengeRepository challengeRepository, ContributionRepository contributionRepository, RandomNumberGenerator randomNumberGenerator) {
        return new ChallengeService(matchDetailsEndpoint, challengeRepository, contributionRepository, randomNumberGenerator);
    }

    @Bean
    public RandomNumberGenerator randomNumberGenerator() {
        return (inclusiveMin, exclusiveMax) -> ThreadLocalRandom.current().nextInt(inclusiveMin, exclusiveMax);
    }

    // Smite data

    @Bean
    public SmiteGodsDataProvider smiteGodsDataProvider(SmiteChampionRepository smiteChampionRepository) {
        return new SmiteGodsDataProvider(smiteChampionRepository);
    }

    @Bean
    public ChampionRepository championRepository(SmiteGodsDataProvider smiteGodsDataProvider) {
        return smiteGodsDataProvider;
    }

    @Bean
    GameRulesProvider gameRulesProvider(GodsRepository godsRepository) {
        return new SmiteRulesProvider(godsRepository);
    }

    @Bean
    public GodsDataProvider godsDataProvider(SmiteGodsDataProvider smiteGodsDataProvider) {
        return smiteGodsDataProvider;
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
    public ContributionRepository contributionRepository(Connection connection) throws SQLException {
        return new ContributionDAO(connection);
    }

    @Bean
    public Connection connection(DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }

    @Bean
    @Singleton
    public DataSource dataSource(
            final @Value("${db.url}") String url,
            final @Value("${db.username}") String username,
            final @Value("${db.password}") String password
    ) throws PropertyVetoException {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl(String.format("jdbc:%s?useLegacyDatetimeCode=false&serverTimezone=UTC", url));
        dataSource.setUser(username);
        dataSource.setPassword(password);

        return dataSource;
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
            final Moshi moshi,
            final SessionStorage sessionStorage
            ) {
        return SmiteApiClient.builder()
                .endpointUrl("http://api.smitegame.com/smiteapi.svc")
                .devId(devId)
                .authKey(authKey)
                .httpClient(httpClient)
                .moshi(moshi)
                .sessionStorage(sessionStorage)
                .build();
    }

    @Bean
    @Singleton
    public SessionStorage sessionStorage(Jedis jedis) {
        return new RedisStorage(jedis);
    }

    @Bean
    @Singleton
    public Jedis jedis() {
        return new Jedis();
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
