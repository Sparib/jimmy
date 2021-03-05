package org.sparib.jimmy.handlers;

import org.sparib.jimmy.classes.Command;
import org.sparib.jimmy.main.Bot;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandHandler {
    public Map<String, Command> commands = new HashMap<>();

    private final URI commandsPath;
    private final String packagePrefix = "org.sparib.jimmy.commands.";
    private final boolean isJar;

    public CommandHandler() {
        URI uri = null;
        try {
            uri = Bot.class.getResource("/org/sparib/jimmy/commands").toURI();
        } catch (Exception e) {
            Bot.logHandler.LOGGER.fatal("Error in getting commands path");
            Bot.errorHandler.printStackTrace(e);
            System.exit(1);
        }

        commandsPath = uri;
        isJar = uri.getScheme().equals("jar");
    }

    public void readCommands() {
        Path dirPath = null;
        FileSystem fs = null;
        if (!isJar) {
            dirPath = Paths.get(commandsPath).toAbsolutePath().normalize();
        } else {
            try {
                final Map<String, String> env = new HashMap<>();
                final String[] array = commandsPath.toString().split("!");
                fs = FileSystems.newFileSystem(URI.create(array[0]), env);
                dirPath = fs.getPath(array[1]);
            } catch (Exception e) {
                Bot.logHandler.LOGGER.fatal("Error in creating filesystem");
                Bot.errorHandler.printStackTrace(e);
                System.exit(1);
            }
        }
        try (Stream<Path> paths = Files.walk(dirPath, 1)) {
            paths.map(Path::toString).filter(file -> file.endsWith(".class"))
                    .forEach(this::addCommand);
        } catch (Exception e) {
            Bot.logHandler.LOGGER.fatal("Error in reading commands!");
            Bot.errorHandler.printStackTrace(e);
            System.exit(1);
        }

        if (fs != null) {
            try {
                fs.close();
            } catch (Exception e) {
                Bot.logHandler.LOGGER.error("Error in closing file system");
                Bot.errorHandler.printStackTrace(e);
            }
        }
    }

    private void addCommand(String path) {
        Command command = null;
        String[] pathParts = path.replace("\\", "/").split("/");
        if (pathParts[pathParts.length - 1].equals("Command.class")) { return; }
        path = packagePrefix + pathParts[pathParts.length - 1].replace(".class", "");
        try {
            command = (Command) Class.forName(path).getConstructor().newInstance();
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
