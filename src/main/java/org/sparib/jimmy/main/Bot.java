package org.sparib.jimmy.main;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.sparib.jimmy.classes.Id;
import org.sparib.jimmy.handlers.*;

import javax.security.auth.login.LoginException;

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
    }

    public static LogHandler getLogHandler() {
        return logHandler;
    }
}
