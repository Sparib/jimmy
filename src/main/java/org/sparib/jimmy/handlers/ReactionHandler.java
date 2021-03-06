package org.sparib.jimmy.handlers;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.sparib.jimmy.main.Bot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ReactionHandler extends ListenerAdapter {
    private final Map<String, Map<String, Role>> messages = new HashMap<>();

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getUser() == Bot.client.getSelfUser()) { return; }
        String messageId = event.getMessageId();

        if (!messages.containsKey(messageId)) { return; }

        String emoji = event.getReactionEmote().getName();

        if (!messages.get(messageId).containsKey(emoji)) { return; }

        Role role = messages.get(messageId).get(emoji);
        Guild guild = event.getGuild();

        guild.addRoleToMember(event.getMember(), role).queue();
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (event.getUser() == Bot.client.getSelfUser()) { return; }
        String messageId = event.getMessageId();

        if (!messages.containsKey(messageId)) { return; }

        String emoji = event.getReactionEmote().getName();

        if (!messages.get(messageId).containsKey(emoji)) { return; }

        Role role = messages.get(messageId).get(emoji);
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

        for (String messageId : messages.keySet()) {
            if (eventId.equals(messageId)) {
                Bot.configHandler.removeRoleMenu(event.getGuild().getId(), event.getChannel().getId(), messageId);
                messages.remove(messageId);
                break;
            }
        }
    }

    public void addReactionMessage(String messageId, Map<String, Role> roleStringMap) {
        messages.put(messageId, roleStringMap);
    }
}
