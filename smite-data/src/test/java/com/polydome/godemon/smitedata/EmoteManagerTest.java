package com.polydome.godemon.smitedata;

import com.polydome.godemon.smitedata.endpoint.EmoteEndpoint;
import com.polydome.godemon.smitedata.entity.Emote;
import com.polydome.godemon.smitedata.entity.EmoteHost;
import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.entity.HostedEmote;
import com.polydome.godemon.smitedata.repository.EmoteRepository;
import com.polydome.godemon.smitedata.repository.GodsRepository;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmoteManagerTest {
    List<Emote> emoteRepositoryData = new LinkedList<>();

    EmoteEndpoint emoteEndpointStub = new EmoteEndpoint() {
        @Override
        public Single<List<HostedEmote>> fetchEmotesFromHost(long guildId) {
            return Single.just(
                    Arrays.asList(
                            new HostedEmote(10, "horus"),
                            new HostedEmote(20, "tyr"),
                            new HostedEmote(30, "flavia")
                    )
            );
        }
    };

    EmoteRepository emoteRepositoryStub = new EmoteRepository() {
        @Override
        public Completable insert(Emote emote) {
            return Completable.create(emitter -> {
                        emoteRepositoryData.add(emote);
                        emitter.onComplete();
                    }
            );
        }
    };

    GodsRepository godsRepositoryStub = new GodsRepository() {
        @Override
        public Maybe<God> findByName(String name) {
            return Maybe.create(emitter -> {
                God god = switch (name) {
                    case "horus" -> new God(1, "horus", "Horus");
                    case "tyr" -> new God(2, "tyr", "Tyr");
                    case "flavia" -> new God(3, "flavia", "Flavia");
                    default -> null;
                };

                if (god == null)
                    emitter.onComplete();
                else
                    emitter.onSuccess(god);
            });
        }
    };

    @Test
    public void hostProvidesEmote_repositoryIsPopulated() {
        EmoteManager SUT = new EmoteManager(emoteEndpointStub, emoteRepositoryStub, godsRepositoryStub);

        SUT.updateEmoteStorage(new EmoteHost(1, 1)).subscribe(
                () -> System.out.println(emoteRepositoryData)
        );
    }
}