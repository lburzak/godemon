package com.polydome.godemon.dbtools;

import com.polydome.godemon.data.dao.EmoteDAO;
import com.polydome.godemon.data.dao.EmoteHostDAO;
import com.polydome.godemon.data.dao.GodDAO;
import com.polydome.godemon.discordapi.EmoteEndpointImpl;
import com.polydome.godemon.smiteapi.GodsEndpointImpl;
import com.polydome.godemon.smiteapi.SmiteApiClient;
import com.polydome.godemon.smitedata.EmoteManager;
import com.polydome.godemon.smitedata.GodManager;
import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.endpoint.EmoteHostNotAvailableException;
import com.polydome.godemon.smitedata.repository.EmoteHostRepository;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import com.squareup.moshi.Moshi;
import io.reactivex.Completable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBTools {
    private final static Logger logger = LoggerFactory.getLogger("DBTools");

    private static Connection createDatabaseConnection(String url, String user, String password) throws SQLException {
        return DriverManager
                .getConnection(String.format("jdbc:%s?useLegacyDatetimeCode=false&serverTimezone=UTC", url), user, password);
    }

    private static String demandEnv(String name) {
        String env = System.getenv(name);
        if (env == null || env.isBlank()) {
            System.err.println(String.format("Missing environmental variable: %s. Exiting.", name));
            System.exit(1);
        } else {
            return env;
        }

        return null;
    }

    private static SmiteApiClient createApiClient() {
        String devId = demandEnv("SMITE_API_DEV_ID");
        String authKey = demandEnv("SMITE_API_AUTH_KEY");
        return SmiteApiClient.builder()
                .endpointUrl("http://api.smitegame.com/smiteapi.svc")
                .devId(devId)
                .authKey(authKey)
                .httpClient(new OkHttpClient())
                .moshi(new Moshi.Builder().build())
                .build();
    }

    private static JDA createJDA() throws LoginException {
        return JDABuilder
                .createLight(demandEnv("GODEMON_BOT_TOKEN"),
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS)
                .enableCache(CacheFlag.EMOTE)
                .build();
    }

    private static CommandLine createCommandLine(String[] args) throws ParseException {
        Options options = new Options();
        options.addRequiredOption("d", "db", true, "database JDBC url");
        options.addRequiredOption("u", "user", true, "database user");
        options.addRequiredOption("p", "password", true, "database password");

        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    public static void main(String[] args) throws SQLException, LoginException, ParseException {
        CommandLine cmd = createCommandLine(args);

        JDA jda = createJDA();
        var dbConnection = createDatabaseConnection(cmd.getOptionValue('d'), cmd.getOptionValue('u'), cmd.getOptionValue('p'));
        var smiteApiClient = createApiClient();
        GodsRepository godRepository = new GodDAO(dbConnection);
        var godEndpoint = new GodsEndpointImpl(smiteApiClient);
        EmoteEndpoint emoteEndpoint = new EmoteEndpointImpl(jda);
        EmoteRepository emoteRepository = new EmoteDAO(dbConnection);
        EmoteHostRepository emoteHostRepository = new EmoteHostDAO(dbConnection);
        var godManager = new GodManager(godEndpoint, godRepository);
        var emoteManager = new EmoteManager(emoteEndpoint, emoteRepository, godRepository, emoteHostRepository);

        smiteApiClient.initialize()
            .andThen(godManager.updateKnownGods())
            .andThen(emoteManager.updateKnownEmotes())
            .andThen(Completable.fromRunnable(jda::shutdownNow))
            .subscribe(() -> {}, (throwable -> {
                if (throwable instanceof EmoteHostNotAvailableException)
                    logger.warn("Host unavailable: `{}`", ((EmoteHostNotAvailableException) throwable).getGuildId());
            }));
    }
}
