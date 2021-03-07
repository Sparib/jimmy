package org.sparib.jimmy.handlers;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.sparib.jimmy.classes.Pingable;
import org.sparib.jimmy.main.Bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReactionHandler extends ListenerAdapter {
    private final Map<String, Map<String, Role>> roleMessages = new HashMap<>();
    private final Map<Message, List<MessageEmbed>> pageMessages = new HashMap<>();

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser() == Bot.client.getSelfUser()) { return; }
        String messageId = event.getMessageId();

        if (roleMessages.containsKey(messageId)) {
            String emoji = event.getReactionEmote().getName();

            if (!roleMessages.get(messageId).containsKey(emoji)) {
                return;
            }

            Role role = roleMessages.get(messageId).get(emoji);
            Guild guild = event.getGuild();

            guild.addRoleToMember(event.getMember(), role).queue();
        } else {
            boolean isPageMessage = false;
            Message message = null;
            for (Message msg : pageMessages.keySet()) {
                if (msg.getId().equalsIgnoreCase(messageId)) {
                    isPageMessage = true;
                    message = msg;
                    break;
                }
            }

            if (isPageMessage) {
                if (!event.getUser().equals(Bot.client.getUserById(Bot.sparibToken.getId()))) { return; }

                List<MessageEmbed> pages = pageMessages.get(message);

                for (int i = 0; i < pages.size(); i++) {
                    MessageEmbed embed = pages.get(i);
                    if (Objects.equals(message.getEmbeds().get(message.getEmbeds().size() - 1).getTitle(), embed.getTitle())) {
                        Bot.logHandler.LOGGER.info(i);
                        Bot.logHandler.LOGGER.info(message.getEmbeds().get(0).getTitle());
                        Bot.logHandler.LOGGER.info(embed.getTitle());
                        if (event.getReaction().getReactionEmote().getName().equals("\u2B05\uFE0F")) {
                            if (i == pages.size() - 1) { return; }
                            message.editMessage(pages.get(i + 1)).complete();
                        } else if (event.getReaction().getReactionEmote().getName().equals("\u27A1\uFE0F")) {
                            if (i == 0) { return; }
                            message.editMessage(pages.get(i - 1)).complete();
                        }
                    }
                }

                pageMessages.put(message, pages);
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() == Bot.client.getSelfUser()) { return; }
        String messageId = event.getMessageId();

        if (!roleMessages.containsKey(messageId)) { return; }

        String emoji = event.getReactionEmote().getName();

        if (!roleMessages.get(messageId).containsKey(emoji)) { return; }

        Role role = roleMessages.get(messageId).get(emoji);
        Guild guild = event.getGuild();

        if (event.getMember() == null) {
            Bot.errorHandler.error(event.getChannel());
            throw new RuntimeException("event.getMember() returned null");
        }
        guild.removeRoleFromMember(event.getMember(), role).queue();
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        String eventId = event.getMessageId();

        for (String messageId : roleMessages.keySet()) {
            if (eventId.equals(messageId)) {
                Bot.configHandler.removeRoleMenu(event.getGuild().getId(), event.getChannel().getId(), messageId);
                roleMessages.remove(messageId);
                break;
            }
        }
    }

    public void addReactionMessage(String messageId, Map<String, Role> roleStringMap) {
        roleMessages.put(messageId, roleStringMap);
    }

    public void addPageMessage(Message message, List<MessageEmbed> messageEmbeds) {
        pageMessages.put(message, messageEmbeds);

        message.addReaction("\u2B05\uFE0F").queue();
        message.addReaction("\u27A1\uFE0F").queue();
    }
}
