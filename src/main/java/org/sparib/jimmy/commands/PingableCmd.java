package org.sparib.jimmy.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.classes.CommandType;
import org.sparib.jimmy.classes.Pingable;
import org.sparib.jimmy.classes.PingType;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return new String[]{"c, name, url, ping time (ms), type (HTTP | TCP), channel",
                            "\nlist (l)"};
    }

    private final Map<Long, List<Pingable>> pingablesPerServer = new HashMap<>();
    private CommandType commandType = null;

    @Override
    public void execute(Message message, String[] args) {
        if (args.length == 0) {
            sendErrorEmbed("Not enough arguments", message);
        } else if (!checkArgs(args)) {
            sendErrorEmbed("Incorrect arguments", message);
        }

        if (commandType.equals(CommandType.CREATE)) {
            List<Pingable> pingables = null;
            for (long serverId : pingablesPerServer.keySet()) {
                if (serverId != message.getIdLong()) { continue; }
                pingables = pingablesPerServer.get(serverId);
                break;
            }

            if (pingables == null) { pingables = new ArrayList<>(); }

            /*
                Args are as follows
                when 0 = c/create
                1: name
                2: url
                3: time
                4: type
                5: channel
             */

            int pingTime = 0;
            try {
                pingTime = Integer.parseInt(args[3]);
            } catch (Exception ignored) {
                sendErrorEmbed("Ping time not an int", message);
                return;
            }

            PingType pingType = null;
            try {
                pingType = PingType.valueOf(args[4]);
            } catch (Exception ignored) {
                sendErrorEmbed("Ping type not correct type", message);
                return;
            }

            TextChannel channel = null;
            if (args[5].toLowerCase().matches("<#[0-9]{18}>")) {
                String channelId = args[5].replace("<#", "").replace(">", "");
                channel = Bot.client.getTextChannelById(channelId);
            } else {
                sendErrorEmbed("Channel input is not a channel", message);
                return;
            }

            assert pingType != null;
            assert channel != null;

            Pingable pingable = new Pingable(args[1], args[2], pingTime, pingType, channel, message);

            pingables.add(pingable);

            pingablesPerServer.put(message.getIdLong(), pingables);

            pingable.startPing();
        }
    }

    private boolean checkArgs(String[] args) {
        boolean createCorrect =
                args.length == 6 && (args[0].toLowerCase().matches("^(c|create)$"));
        boolean deleteCorrect =
                args.length == 2 && (args[0].toLowerCase().matches("^(d|delete)$"));
        boolean listCorrect =
                args.length == 1 && (args[0].toLowerCase().matches("^(l|list)$"));

        if (createCorrect) {
            commandType = CommandType.CREATE;
        } else if (deleteCorrect) {
            commandType = CommandType.DELETE;
        } else {
            commandType = CommandType.LIST;
        }

        return createCorrect || deleteCorrect || listCorrect;
    }

    private void sendErrorEmbed(String reason, Message message) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Error")
                .setDescription(String.format("Arguments are incorrect.\n`%s`", reason))
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .setFooter("Command attempted by " + message.getAuthor().getAsTag())
                .build();
        message.getChannel().sendMessage(embed).complete();
    }
}
