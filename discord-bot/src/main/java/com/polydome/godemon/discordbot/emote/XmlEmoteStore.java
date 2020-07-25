package com.polydome.godemon.discordbot.emote;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.inject.Inject;
import javax.xml.parsers.SAXParser;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class XmlEmoteStore implements EmoteStore {
    private final Map<String, Long> data = new HashMap<>();
    private final SAXParser saxParser;

    @Inject
    public XmlEmoteStore(SAXParser saxParser) {
        this.saxParser = saxParser;
    }

    public Single<Set<String>> load(InputStream inputStream) {
        return Single.create(emitter -> {
            saxParser.parse(inputStream, new DefaultHandler() {
                private String name;
                private boolean isEmoteOpen;
                private boolean isIdOpen;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if (qName.equals("emote")) {
                        name = attributes.getValue("name");
                        isEmoteOpen = true;
                    }

                    if (qName.equals("id") && isEmoteOpen)
                        isIdOpen = true;
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if (qName.equals("emote"))
                        isEmoteOpen = false;

                    if (qName.equals("id"))
                        isIdOpen = false;
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    if (isEmoteOpen && isIdOpen)
                        data.put(name, Long.parseLong(new String(ch, start, length)));
                }
            });

            emitter.onSuccess(data.keySet());
        });
    }

    @Override
    public long findId(String name) {
        return data.get(name);
    }

    @Override
    public boolean has(String name) {
        return data.containsKey(name);
    }
}
