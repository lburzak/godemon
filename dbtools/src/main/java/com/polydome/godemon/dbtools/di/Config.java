package com.polydome.godemon.dbtools.di;

import com.polydome.godemon.data.dao.EmoteDAO;
import com.polydome.godemon.data.dao.EmoteHostDAO;
import com.polydome.godemon.data.dao.GodDAO;
import com.polydome.godemon.discordapi.EmoteEndpointImpl;
import com.polydome.godemon.smiteapi.client.SmiteApiClient;
import com.polydome.godemon.smitedata.EmoteManager;
import com.polydome.godemon.smitedata.GodManager;
import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.endpoint.GodEndpoint;
import com.polydome.godemon.smitedata.implementation.SmiteGodsEndpoint;
import com.polydome.godemon.smitedata.repository.EmoteHostRepository;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import com.polydome.godemon.smitedata.repository.GodsRepository;
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
import org.springframework.context.annotation.*;

import javax.inject.Singleton;
import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@PropertySource("file:application.properties")
@ComponentScan(basePackages = "com.polydome.godemon.dbtools.app")
public class Config {
    @Bean
    @Scope("prototype")
    public Logger logger(final InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
    }

    @Bean
    @Singleton
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    @Singleton
    public Moshi moshi() {
        return new Moshi.Builder().build();
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
    public JDA jda(final @Value("${discord.botToken}") String botToken) throws LoginException {
        return JDABuilder
                .createLight(botToken, GatewayIntent.GUILD_EMOJIS)
                .enableCache(CacheFlag.EMOTE)
                .build();
    }

    @Bean
    public GodManager godManager(final GodEndpoint godEndpoint, final GodsRepository godsRepository) {
        return new GodManager(godEndpoint, godsRepository);
    }

    @Bean
    public EmoteManager emoteManager(
            final EmoteEndpoint emoteEndpoint,
            final EmoteRepository emoteRepository,
            final GodsRepository godsRepository,
            final EmoteHostRepository emoteHostRepository
    ) {
        return new EmoteManager(emoteEndpoint, emoteRepository, godsRepository, emoteHostRepository);
    }

    @Bean
    public GodEndpoint godEndpoint(SmiteApiClient smiteApiClient) {
        return new SmiteGodsEndpoint(smiteApiClient);
    }

    @Bean
    public GodsRepository godsRepository(Connection connection) throws SQLException {
        return new GodDAO(connection);
    }

    @Bean
    public EmoteEndpoint emoteEndpoint(JDA jda) {
        return new EmoteEndpointImpl(jda);
    }

    @Bean
    public EmoteRepository emoteRepository(Connection connection) throws SQLException {
        return new EmoteDAO(connection);
    }

    @Bean
    public EmoteHostRepository emoteHostRepository(Connection connection) throws SQLException {
        return new EmoteHostDAO(connection);
    }
}
