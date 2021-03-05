package org.sparib.jimmy.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.time.Instant;

public class Prefix extends Command {
    @Override
    public String name() {
        return "Prefix";
    }

    @Override
    public String description() {
        return "Get or set the prefix for the server";
    }

    @Override
    public String[] callers() {
        return new String[]{"prefix"};
    }

    @Override
    public String[] args() {
        return new String[0];
    }

    @Override
    public void execute(Message message, String[] args) {
        if (args.length > 0) {
            if (Bot.configHandler.setPrefix(message, args[0])) {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Prefix Change Successful!")
                        .setDescription("Successfully change this server's prefix to `" + args[0] + "`")
                        .setColor(Color.GREEN)
                        .setTimestamp(Instant.now())
                        .setFooter("Command issued by " + message.getAuthor().getAsTag())
                        .build();

                message.getChannel().sendMessage(embed).complete();
            }

            return;
        }

        String prefix = Bot.configHandler.getPrefix(message);

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Prefix")
                .setDescription("The current prefix is `" + prefix + "`")
                .setFooter("Command issued by " + message.getAuthor().getName())
                .setTimestamp(Instant.now())
                .build();

        message.getChannel().sendMessage(embed).complete();
    }
}
