package org.sparib.jimmy.commands;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.apache.logging.log4j.Logger;
import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.handlers.LogHandler;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class RoleMenu extends Command {
    @Override
    public String name() {
        return "Role Menu";
    }

    @Override
    public String description() {
        return "Make a reaction role menu\nThe role/emoji argument is repeated in the pair";
    }

    @Override
    public String[] callers() {
        return new String[]{"rolemenu", "rm"};
    }

    @Override
    public String[] args() {
        return new String[]{"title", "role", "emoji"};
    }

    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private final LogHandler logHandler = Bot.getLogHandler();
    private final Logger LOGGER = logHandler.LOGGER;
    private final Pattern emojiPattern = Pattern.compile("(U\\+[A-Z0-9]{5})+");

    @Override
    public void execute(Message message, String[] args) {
        embedBuilder.clear();
        if (args.length % 2 == 0 || args.length == 1) {
            embedBuilder.setTitle("Error!")
                    .setColor(Color.RED)
                    .setDescription("The number of arguments is not correct!")
                    .setTimestamp(Instant.now())
                    .setFooter("Command issued by " + message.getAuthor().getAsTag());
            message.getChannel().sendMessage(embedBuilder.build()).complete();
            return;
        }

        boolean areRoles = true;
        for (int i = 1; i < args.length; i += 2) {
            if (!args[i].matches("<@&[0-9]{18}>")) {
                areRoles = false;
                break;
            }
        }
        if (!areRoles) {
            embedBuilder.setTitle("Error!")
                    .setColor(Color.RED)
                    .setDescription("There is an incorrect role!\n" +
                                    "(You didn't mention a role for one of the role arguments)")
                    .setTimestamp(Instant.now())
                    .setFooter("Command issued by " + message.getAuthor().getAsTag());
            message.getChannel().sendMessage(embedBuilder.build()).complete();
            return;
        }

        boolean areEmoji = true;
        for (int i = 2; i < args.length; i += 2) {
            if (EmojiParser.extractEmojis(args[i]).size() == 0 &&
                    !emojiPattern.matcher("U+" + Integer.toHexString(
                            Character.codePointAt(args[i], 0)).toUpperCase()).find()) {
                Bot.logHandler.LOGGER.info(args[i]);
                areEmoji = false;
                break;
            }
        }
        if (!areEmoji) {
            embedBuilder.setTitle("Error!")
                    .setColor(Color.RED)
                    .setDescription("There is an incorrect emoji!\n" +
                            "(You messed up an emoji for one of the emoji arguments)")
                    .setTimestamp(Instant.now())
                    .setFooter("Command issued by " + message.getAuthor().getAsTag());
            message.getChannel().sendMessage(embedBuilder.build()).complete();
            return;
        }

        String title = args[0];
        Map<String, Role> stringRoleMap = new LinkedHashMap<>();

        for (int i = 1; i < args.length - 1; i += 2) {
            String roleId = args[i].replace("<@&", "").replace(">", "");
            Role role = Bot.client.getRoleById(roleId);
            String emoji = args[i + 1];
            stringRoleMap.put(emoji, role);
            assert role != null;
        }

        embedBuilder.setTitle(title);

        stringRoleMap.forEach((String emoji, Role role) -> embedBuilder.addField(role.getName(), emoji, true));

        Consumer<Message> callback = (m) -> {
            stringRoleMap.forEach((String emoji, Role role) -> m.addReaction(emoji).queue());

            Bot.reactionHandler.addReactionMessage(m.getId(), stringRoleMap);

            Bot.configHandler.addRoleMenu(m, stringRoleMap);
        };

        message.getChannel().sendMessage(embedBuilder.build()).queue(callback);
    }
}
