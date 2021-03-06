package org.sparib.jimmy.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.time.Instant;
import java.util.Locale;

public class PingableCmd extends Command {

    @Override
    public String name() {
        return "Pingable";
    }

    @Override
    public String description() {
        return "Create, delete, or list pingables.";
    }

    @Override
    public String[] callers() {
        return new String[]{"pingable", "pg"};
    }

    @Override
    public String[] args() {
        return new String[]{"c/d, pingable name",
                            "\nlist (l)"};
    }

    @Override
    public void execute(Message message, String[] args) {
        if (args.length == 0) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Arguments are incorrect.\n`Not enough arguments`")
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now())
                    .setFooter("Command attempted by " + message.getAuthor().getAsTag())
                    .build();
            message.getChannel().sendMessage(embed).complete();
            return;
        } else if (!checkArgs(args)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Arguments are incorrect.\n`Incorrect arguments`")
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now())
                    .setFooter("Command attempted by " + message.getAuthor().getAsTag())
                    .build();
            message.getChannel().sendMessage(embed).complete();
            return;
        }
        Bot.logHandler.LOGGER.info("Pingable command received with correct arguments");
    }

    private boolean checkArgs(String[] args) {
        boolean listCorrect =
                args.length == 1 && (args[0].toLowerCase().matches("^(l|list)$"));
        boolean cdCorrect =
                args.length == 2 && (args[0].toLowerCase().matches("^(c|create|d|delete)$"));
        return cdCorrect || listCorrect;
    }
}
