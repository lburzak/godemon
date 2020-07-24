package com.polydome.godemon.discordbot.emote;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

class XmlEmoteStoreTest {
    XmlEmoteStore SUT;
    static SAXParserFactory saxParserFactory;
    static SAXParser saxParser;

    @BeforeAll
    static void setUp() throws ParserConfigurationException, SAXException {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParser = saxParserFactory.newSAXParser();
    }

    @BeforeEach
    public void setUpOne() {
        SUT = new XmlEmoteStore(saxParser);
    }

    @Test
    void testLoad() {
        String xmlContent = "<emotes>\n" +
                "    <emote name=\"join\">\n" +
                "        <id>735488600980062210</id>\n" +
                "    </emote>\n" +
                "    <emote name=\"sync\">\n" +
                "        <id>735567114638852178</id>\n" +
                "    </emote>\n" +
                "    <emote name=\"history\">\n" +
                "        <id>735491281798823959</id>\n" +
                "    </emote>\n" +
                "</emotes>";

        SUT.load(new ByteArrayInputStream(xmlContent.getBytes())).subscribe(() -> {
            assertThat(SUT.findId("history"), equalTo(735491281798823959L));
        });

    }
}