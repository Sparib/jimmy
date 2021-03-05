package org.sparib.jimmy.handlers;

import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.main.Bot;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandHandler {
    public Map<String, Command> commands = new HashMap<>();

    private final String commandsPath = "./src/main/java/org/sparib/jimmy/commands";
    private final String packagePrefix = "org.sparib.jimmy.commands.";

    public void readCommands() {
        Path dirPath = Paths.get(commandsPath).toAbsolutePath().normalize();
        try (Stream<Path> paths = Files.walk(dirPath, 1)) {
            paths.map(Path::toString).filter(file -> file.endsWith(".java"))
                    .forEach(this::addCommand);
        } catch (Exception e) {
            Bot.logHandler.LOGGER.fatal("Error in reading commands!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void addCommand(String path) {
        Command command = null;
        String[] pathParts = path.replace("\\", "/").split("/");
        if (pathParts[pathParts.length - 1].equals("Command.java")) { return; }
        path = packagePrefix + pathParts[pathParts.length - 1].replace(".java", "");
        try {
            command = (Command) Class.forName(path).getDeclaredConstructor().newInstance();
        } catch ( ClassNotFoundException
                | IllegalAccessException
                | InstantiationException
                | NoSuchMethodException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (command == null) { return; }
        for (int i = 0; i < command.callers().length; i++) {
            commands.put(command.callers()[i], command);
        }
    }
}
