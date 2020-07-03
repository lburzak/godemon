package com.polydome.godemon.smitedata;

import com.polydome.godemon.smitedata.endpoint.EmojiEndpoint;
import com.polydome.godemon.smitedata.entity.Emoji;
import com.polydome.godemon.smitedata.entity.EmojiHost;
import com.polydome.godemon.smitedata.entity.God;
import com.polydome.godemon.smitedata.entity.HostedEmoji;
import com.polydome.godemon.smitedata.repository.EmojiRepository;
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
class EmojiManagerTest {
    List<Emoji> emojiRepositoryData = new LinkedList<>();

    EmojiEndpoint emojiEndpointStub = new EmojiEndpoint() {
        @Override
        public Single<List<HostedEmoji>> fetchEmojisFromHost(long guildId) {
            return Single.just(
                    Arrays.asList(
                            new HostedEmoji(10, "horus"),
                            new HostedEmoji(20, "tyr"),
                            new HostedEmoji(30, "flavia")
                    )
            );
        }
    };

    EmojiRepository emojiRepositoryStub = new EmojiRepository() {
        @Override
        public Completable insert(Emoji emoji) {
            return Completable.create(emitter -> {
                        emojiRepositoryData.add(emoji);
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
    public void hostProvidesEmojis_repositoryIsPopulated() {
        EmojiManager SUT = new EmojiManager(emojiEndpointStub, emojiRepositoryStub, godsRepositoryStub);

        SUT.updateEmojiStorage(new EmojiHost(1, 1)).subscribe(
                () -> System.out.println(emojiRepositoryData)
        );
    }
}