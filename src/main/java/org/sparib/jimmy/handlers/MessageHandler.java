package org.sparib.jimmy.handlers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.core.util.ArrayUtils;
import org.sparib.jimmy.main.Bot;

import java.util.List;

public class MessageHandler extends ListenerAdapter {
    private String botId = null;
    private String botMention = null;

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) { return; }
        if (botId == null) {
            botId = Bot.botToken.getId();
            if (botId != null) { botMention = "<@!" + botId + ">"; }
        }
        final Message message = event.getMessage();

        String prefix = Bot.configHandler.getPrefix(message);
        List<User> mentions = message.getMentionedUsers();

        if ((!mentions.contains(Bot.client.getSelfUser()) || !message.getContentRaw().startsWith(botMention)) &&
            !message.getContentRaw().startsWith(prefix)) {
            return;
        }

        String[] contentSplit = message.getContentRaw().split(" ");
        if (contentSplit[0].equalsIgnoreCase(botMention) && contentSplit.length < 2) { return; }
        String command;
        if (contentSplit[0].equalsIgnoreCase(botMention)) { contentSplit = ArrayUtils.remove(contentSplit, 0); }
        command = contentSplit[0].replace(prefix, "").replace(botMention, "");
        String[] args = new String[contentSplit.length - 1];
        if (contentSplit.length > 1) {
            System.arraycopy(contentSplit, 1, args, 0, contentSplit.length - 1);
        }

        if (!Bot.commandHandler.commands.containsKey(command)) { return; }

        Bot.commandHandler.commands.get(command).execute(message, args);
    }
}
