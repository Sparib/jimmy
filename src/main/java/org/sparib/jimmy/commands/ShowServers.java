package org.sparib.jimmy.commands;

import net.dv8tion.jda.api.entities.Message;
import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.main.Bot;

public class ShowServers extends Command {
    @Override
    public String name() {
        return "ShowServers";
    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public String[] callers() {
        return new String[]{"show-servers"};
    }

    @Override
    public String[] args() {
        return new String[0];
    }

    @Override
    public void execute(Message message, String[] args) {
        if (!message.getAuthor().getId().equals(Bot.sparibToken.getId())) {
            Bot.logHandler.LOGGER.warn("Got [Show Servers] command from someone other than Sparib!\n" +
                                        "Sparib ID: " + Bot.sparibToken.getId() + "\n" +
                                        "Someone ID: " + message.getAuthor().getId());
            return;
        }

        Bot.logHandler.success("Got [Show Servers] command from Sparib");
    }

    @Override
    public boolean show() {
        return false;
    }
}
