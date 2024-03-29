package com.polydome.godemon.smiteapi.client;

import com.polydome.godemon.smiteapi.model.*;
import com.squareup.moshi.*;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class SmiteApiClient {
    private final String endpointUrl;
    private final String devId;
    private final String authKey;
    private final OkHttpClient httpClient;
    private final Moshi moshi;
    private final Logger logger;
    private final DateTimeFormatter timestampFormatter;
    private final SessionStorage sessionStorage;

    private static final int SESSION_ALIVE_MINUTES = 15;
    private final String DEFAULT_FORMAT = "json";
    private final LanguageCode DEFAULT_LANG = LanguageCode.ENGLISH;

    private SmiteApiClient(String endpointUrl, String devId, String authKey, OkHttpClient httpClient, Moshi moshi, SessionStorage sessionStorage) {
        this.endpointUrl = endpointUrl;
        this.devId = devId;
        this.authKey = authKey;
        this.httpClient = httpClient;
        this.moshi = moshi;
        this.sessionStorage = sessionStorage;

        logger = LoggerFactory.getLogger(SmiteApiClient.class);
        timestampFormatter = DateTimeFormatter
                .ofPattern("uuuuMMddHHmmss")
                .withZone( ZoneId.of("UTC"));
    }

    private Single<String> getSessionId() {
        return Single.create(emitter -> {
            if (sessionStorage.existsSession()) {
                emitter.onSuccess(sessionStorage.getSessionId());
            } else {
                createSession().subscribe(() -> {
                    emitter.onSuccess(sessionStorage.getSessionId());
                });
            }
        });
    }

    private String parseApiMethod(String method) {
        return method + DEFAULT_FORMAT;
    }

    private String createSignature(String method) {
        String raw = devId + method + authKey + createTimestamp();
        byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
        return DigestUtils.md5Hex(bytes);
    }

    @NonNull
    private String createTimestamp() {
        return timestampFormatter.format(Instant.now());
    }

    public Completable initialize() {
        return createSession();
    }

    private <R> Single<R> performJsonApiCall(Request request, JsonAdapter<R> adapter) {
        return Single.create(emitter -> {
            try (Response response = httpClient.newCall(request).execute()) {
                ResponseBody body = response.body();
                if (body != null) {
                    String bodyContent = body.string();
                    try {
                        R jsonResponse = adapter.lenient().fromJson(bodyContent);
                        if (jsonResponse == null)
                            emitter.onError(new UnexpectedResponseException("Failed to parse JSON", bodyContent));
                        else {
                            emitter.onSuccess(jsonResponse);
                        }
                    } catch (JsonEncodingException e) {
                        emitter.onError(new UnexpectedResponseException("Failed to parse JSON: " + e.getMessage(), bodyContent));
                    } catch (JsonDataException e) {
			emitter.onError(new UnexpectedResponseException("Failed to parse JSON: " + e.getMessage(), bodyContent));
		    }
                } else {
                    emitter.onError(new Exception("Request failed"));
                }
            }
        });
    }

    private HttpUrl.Builder createBaseUrlBuilder(String method) {
        HttpUrl url = HttpUrl.parse(endpointUrl);
        if (url == null)
            return null;

        HttpUrl.Builder urlBuilder = url.newBuilder();

        return urlBuilder
                .addPathSegment(parseApiMethod(method))
                .addPathSegment(devId)
                .addPathSegment(createSignature(method));
    }

    private Request createSimpleRequest(HttpUrl url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

    private Completable createSession() {
        String method = "createsession";

        return Completable.create(emitter -> {
            HttpUrl.Builder urlBuilder = createBaseUrlBuilder(method)
                    .addPathSegment(createTimestamp());

            Request request = createSimpleRequest(urlBuilder.build());
            performJsonApiCall(request, moshi.adapter(CreateSessionResponse.class))
                    .subscribe(response -> {
                        sessionStorage.setSessionId(response.sessionId, SESSION_ALIVE_MINUTES);
                        logger.info("Session created");
                        emitter.onComplete();
                    }, e -> { if (e instanceof UnexpectedResponseException) logger.error("UnexpectedResponse: {}", ((UnexpectedResponseException) e).getRawResponse()); });
        });
    }

    public Maybe<Player> getPlayer(String name) {
        String method = "getplayer";

        return getSessionId().flatMapMaybe(sessionId ->
                Maybe.create(emitter -> {
                    HttpUrl.Builder urlBuilder = createBaseUrlBuilder(method)
                            .addPathSegment(sessionId)
                            .addPathSegment(createTimestamp())
                            .addPathSegment(name);

                    Request request = createSimpleRequest(urlBuilder.build());

                    JsonAdapter<List<Player>> adapter = moshi.adapter(Types.newParameterizedType(List.class, Player.class));
                    performJsonApiCall(request, adapter)
                            .subscribe(response -> {
                                if (response.size() == 0)
                                    emitter.onComplete();
                                else
                                    emitter.onSuccess(response.get(0));
                            });
                })
        );
    }

    public Maybe<List<MatchParticipantStats>> getMatchDetails(int matchId) {
        return getSessionId().flatMapMaybe(sessionId ->
                    Maybe.create(emitter -> {
                HttpUrl.Builder urlBuilder = createBaseUrlBuilder("getmatchdetails")
                        .addPathSegment(sessionId)
                        .addPathSegment(createTimestamp())
                        .addPathSegment(String.valueOf(matchId));

                Request request = createSimpleRequest(urlBuilder.build());

                JsonAdapter<List<MatchParticipantStats>> adapter = moshi.adapter(Types.newParameterizedType(List.class, MatchParticipantStats.class));
                performJsonApiCall(request, adapter)
                        .subscribe(emitter::onSuccess);
            })
        );
    }

    public Maybe<List<RecentMatch>> getMatchHistory(int playerId) {
        return getSessionId().flatMapMaybe(sessionId ->
            Maybe.create(emitter -> {
                HttpUrl.Builder urlBuilder = createBaseUrlBuilder("getmatchhistory")
                        .addPathSegment(sessionId)
                        .addPathSegment(createTimestamp())
                        .addPathSegment(String.valueOf(playerId));

                Request request = createSimpleRequest(urlBuilder.build());

                JsonAdapter<List<RecentMatch>> adapter = moshi.adapter(Types.newParameterizedType(List.class, RecentMatch.class));
                performJsonApiCall(request, adapter)
                        .subscribe(emitter::onSuccess, e -> { if (e instanceof UnexpectedResponseException) logger.error("UnexpectedResponse: {}", ((UnexpectedResponseException) e).getRawResponse()); });
            })
        );
    }

    public Maybe<List<GodDefinition>> getGods() {
        String method = "getgods";

        return getSessionId().flatMapMaybe(sessionId ->
            Maybe.create(emitter -> {
                HttpUrl.Builder urlBuilder = createBaseUrlBuilder(method)
                        .addPathSegment(sessionId)
                        .addPathSegment(createTimestamp())
                        .addPathSegment(String.valueOf(DEFAULT_LANG.id));

                Request request = createSimpleRequest(urlBuilder.build());

                JsonAdapter<List<GodDefinition>> adapter = moshi.adapter(Types.newParameterizedType(List.class, GodDefinition.class));
                performJsonApiCall(request, adapter)
                        .subscribe(emitter::onSuccess);
            })
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String endpointUrl;
        private String devId;
        private String authKey;
        private OkHttpClient httpClient;
        private Moshi moshi;
        private SessionStorage sessionStorage;

        public Builder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public Builder devId(String devId) {
            this.devId = devId;
            return this;
        }

        public Builder authKey(String authKey) {
            this.authKey = authKey;
            return this;
        }

        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder moshi(Moshi moshi) {
            this.moshi = moshi;
            return this;
        }

        public Builder sessionStorage(SessionStorage sessionStorage) {
            this.sessionStorage = sessionStorage;
            return this;
        }

        private String getMissingComponentName() {
            if (endpointUrl == null)
                return "endpointUrl";
            if (devId == null)
                return "devId";
            if (authKey == null)
                return "authKey";
            if (httpClient == null)
                return "httpClient";
            if (moshi == null)
                return "moshi";
            if (sessionStorage == null)
                return "ssesionStorage";
            return null;
        }

        public SmiteApiClient build() throws IllegalStateException {
            String missingComponent = getMissingComponentName();
            if (missingComponent != null)
                throw new IllegalStateException(String.format("Unable to construct without `%s`", missingComponent));
            return new SmiteApiClient(endpointUrl, devId, authKey, httpClient, moshi, sessionStorage);
        }
    }
}
