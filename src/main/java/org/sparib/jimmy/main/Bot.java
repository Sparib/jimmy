package org.sparib.jimmy.main;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.sparib.jimmy.classes.Id;
import org.sparib.jimmy.classes.Pingable;
import org.sparib.jimmy.handlers.*;
import org.w3c.dom.Text;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Bot {
    public static JDA client;
    public static Id sparibToken;
    public static Id botToken = null;

    public static LogHandler logHandler = new LogHandler();
    public static ErrorHandler errorHandler = new ErrorHandler();
    public static ConfigHandler configHandler = new ConfigHandler();
    public static CommandHandler commandHandler = new CommandHandler();
    public static MessageHandler messageHandler = new MessageHandler();
    public static ReactionHandler reactionHandler = new ReactionHandler();

    public static void main(String[] args) throws LoginException {
        Dotenv dotenv = Dotenv.load();
        final String token = dotenv.get("TOKEN");
        sparibToken = new Id(dotenv.get("SPARID"));

        client = JDABuilder.create(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_EMOJIS)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS,
                            CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .setActivity(Activity.playing("the initialization game"))
                    .addEventListeners(configHandler, messageHandler, reactionHandler)
                    .build();

        try {
            client.awaitReady();
        } catch (Exception e) {
            logHandler.LOGGER.error(e.getStackTrace());
        }

        commandHandler.readCommands();
        configHandler.readRoleMenus();

        logHandler.LOGGER.info("Sparib's token is: " + sparibToken.getId());

        client.getPresence().setActivity(Activity.watching("all of your messages"));

        botToken = new Id(client.getSelfUser().getId());

        logHandler.success("Login and Initialization Successful!");

        TextChannel channel = client.getTextChannelById(697159933518676032L);
        assert channel != null;
        final Message[] message = new Message[1];
        MessageEmbed embed1 = new EmbedBuilder().setTitle("Embed 1").build();
        MessageEmbed embed2 = new EmbedBuilder().setTitle("Embed 2").build();
        List<MessageEmbed> embeds = new LinkedList<>();
        embeds.add(embed1);
        embeds.add(embed2);
        channel.sendMessage(embed1).queue(m -> reactionHandler.addPageMessage(m, embeds));
    }

    public static LogHandler getLogHandler() {
        return logHandler;
    }
}
