package org.sparib.jimmy.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class ErrorHandler {
    private final EmbedBuilder errorEmbed;
    private final DateTimeFormatter errorTimestampFormat;

    public ErrorHandler() {
        errorEmbed = new EmbedBuilder()
                .setTitle("An Unexpected Error Occured!")
                .setColor(Color.RED)
                .setDescription("Please DM Sparib#9710 with what you were doing beforehand, and the following timestamp.");
        errorTimestampFormat = DateTimeFormatter.ofPattern("'['uuuu-MM-dd HH:mm:ss,SSS']'").withZone(ZoneId.from(ZoneOffset.UTC));
    }

    public void error(@NotNull Guild guild) {
        setTime();
        Objects.requireNonNull(guild.getDefaultChannel()).sendMessage(errorEmbed.build()).complete();
    }

    public void error(MessageChannel channel) {
        setTime();
        channel.sendMessage(errorEmbed.build()).complete();
    }

    public void error(TextChannel channel) {
        setTime();
        channel.sendMessage(errorEmbed.build()).complete();
    }

    public void printStackTrace(Exception e) {
        final StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().toString().replace("class ", ""))
                .append(": ").append(e.getMessage());

        Arrays.stream(e.getStackTrace()).forEach(
                (StackTraceElement element) -> sb.append("\n\tat ").append(element.toString()));

        Bot.logHandler.LOGGER.error(sb.toString());
    }

    private void setTime() {
        errorEmbed.setFooter(errorTimestampFormat.format((Instant.now())));
    }
}
