package org.sparib.jimmy.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Help extends Command {
    @Override
    public String name() {
        return "Help";
    }

    @Override
    public String description() {
        return "Shows this help menu";
    }

    @Override
    public String[] callers() {
        return new String[]{"help", "info"};
    }

    @Override
    public String[] args() {
        return new String[0];
    }

    @Override
    public void execute(Message message, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        Map<String, Command> commands = Bot.commandHandler.commands;

        int i = -1;
        ArrayList<Command> commandTable = new ArrayList<>();
        boolean duplicate;
        for (Command command : commands.values()) {
            if (!command.show()) { continue; }
            duplicate = false;
            if (i != -1) {
                for (Command curCommand : commandTable) {
                    if (curCommand == command) {
                        duplicate = true;
                        break;
                    }
                }
            }

            if (duplicate) { continue; }

            i++;

            commandTable.add(command);
            
            // Get command attributes
            String name = command.name();
            String description = command.description();
            String[] callers = command.callers();
            String[] arguments = command.args();

            // Get callers
            StringBuilder commandData = new StringBuilder(description + "\n__Callers:__```");
            for (int k = 0; k < callers.length; k++) {
                commandData.append(callers[k]);
                if (k != callers.length - 1) { commandData.append(", "); }
            }
            commandData.append("```");

            // Get arguments
            if (arguments.length > 0) {
                commandData.append("__Arguments:__```");
                for (int a = 0; a < arguments.length; a++) {
                    commandData.append(arguments[a]);
                    if (a != arguments.length - 1) { commandData.append(", "); }
                }
                commandData.append("```");
            }
            
            MessageEmbed.Field commandField = new MessageEmbed.Field(name, commandData.toString(), false);
            embed.addField(commandField);
        }

        // Add constants
        embed.setColor(Color.CYAN)
                .setFooter("Command issued by " + message.getAuthor().getAsTag())
                .setTitle("Help Section")
                .setTimestamp(new Date().toInstant());

        // Send Embed
        message.getChannel().sendMessage(embed.build()).queue();
    }
}
