package org.sparib.jimmy.tests;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparib.jimmy.main.Bot;
import org.springframework.web.util.HtmlUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

public class TestLogger {
    private static final Logger LOGGER = LogManager.getLogger(TestLogger.class);

    public static void main(String[] args) {
        LOGGER.trace("Trace Message!");
        LOGGER.debug("Debug Message!");
        LOGGER.info("Info Message!");
        LOGGER.log(Level.forName("SUCCESS", 350), "Success Message!");
        LOGGER.warn("Warn Message!");
        LOGGER.error("Error Message!");
        LOGGER.fatal("Fatal Message!");

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("'['uuuu-MM-dd HH:mm:ss,SSS']'").withZone(ZoneId.from(ZoneOffset.UTC));
        String date = dateFormat.format(Instant.now());
        LOGGER.info(date);

        Exception e = new Exception("asd");
        Bot.errorHandler.printStackTrace(e);

        String html = "🦽";
        LOGGER.info(html);
        LOGGER.info(HtmlUtils.htmlEscapeDecimal(html));
        LOGGER.info("\uD83E\uDDBD".replace("\\u", "U+").matches("(U+[A-Z0-9])+"));
        int res = Character.codePointAt(html, 0);
        String uThing = "U+" + Integer.toHexString(res).toUpperCase();
        LOGGER.info(uThing);
        LOGGER.info(uThing.matches("(U\\+[A-Z0-9]{5})+"));
    }
}
