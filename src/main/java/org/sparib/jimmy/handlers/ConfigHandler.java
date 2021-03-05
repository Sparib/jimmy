package org.sparib.jimmy.handlers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.sparib.jimmy.main.Bot;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class ConfigHandler extends ListenerAdapter {
    private final Path defaultConfigPath = Paths.get("./src/main/resources/default_config.xml");
    private final String resourcesPath = "./src/main/resources/";
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        checkGuildExist(guild);
    }

    public String getPrefix(Message message) {
        Document document = null;
        try {
            Path guildPath = getGuildPath(message.getGuild().getId());
            File guildFile = new File(guildPath.toString());
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(guildFile);
        } catch (Exception e) {
            Bot.errorHandler.error(message.getChannel());
            e.printStackTrace();
        }
        if (document == null) { return ""; }
        return document.getElementsByTagName("Prefix").item(0).getTextContent();
    }

    public boolean setPrefix(Message message, String prefix) {
        Path guildPath = getGuildPath(message.getGuild().getId());
        File guildFile = new File(guildPath.toString());
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document newDocument = db.newDocument();
            Document currentDocument = db.parse(guildFile);

            NodeList roleMenus = currentDocument.getElementsByTagName("RoleMenus");
            Node roleMenusImported = newDocument.importNode(roleMenus.item(0), true);
            Bot.logHandler.LOGGER.info(roleMenus.getLength());

            Element rootEle = newDocument.createElement("Config");

            Element prefixEle = newDocument.createElement("Prefix");
            prefixEle.appendChild(newDocument.createTextNode(prefix));
            rootEle.appendChild(prefixEle);

            rootEle.appendChild(roleMenusImported);

            newDocument.appendChild(rootEle);

            beautifyDocument(newDocument);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setParameter("indent-number", "4");

            tr.transform(new DOMSource(newDocument), new StreamResult(new FileOutputStream(guildFile)));
        } catch (Exception e) {
            Bot.errorHandler.error(message.getChannel());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void addRoleMenu(Message message, Map<String, Role> stringRoleMap) {
        Path guildPath = getGuildPath(message.getGuild().getId());
        File guildFile = new File(guildPath.toUri());

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document newDocument = db.newDocument();
            Document currentDocument = db.parse(guildFile);

            Element rootEle = newDocument.createElement("Config");

            NodeList prefix = currentDocument.getElementsByTagName("Prefix");
            Node prefixImported = newDocument.importNode(prefix.item(0), true);
            rootEle.appendChild(prefixImported);

            NodeList roleMenus = currentDocument.getElementsByTagName("RoleMenus");
            Node roleMenusImported = newDocument.importNode(roleMenus.item(0), true);

            Element roleMenu = newDocument.createElement("RoleMenu");

            Element roleMenuId = newDocument.createElement("Id");
            roleMenuId.appendChild(newDocument.createTextNode(message.getId()));
            roleMenu.appendChild(roleMenuId);

            stringRoleMap.forEach((String emoji, Role role) -> {
                Element roleMenuSelection = newDocument.createElement("Selection");

                Element selectionRoleId = newDocument.createElement("RoleId");
                selectionRoleId.appendChild(newDocument.createTextNode(role.getId()));
                roleMenuSelection.appendChild(selectionRoleId);

                Element selectionEmoji = newDocument.createElement("Emoji");
                selectionEmoji.appendChild(newDocument.createTextNode(emoji));
                roleMenuSelection.appendChild(selectionEmoji);

                roleMenu.appendChild(roleMenuSelection);
            });

            roleMenusImported.appendChild(roleMenu);

            rootEle.appendChild(roleMenusImported);

            newDocument.appendChild(rootEle);

            beautifyDocument(newDocument);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setParameter("indent-number", "4");

            tr.transform(new DOMSource(newDocument), new StreamResult(new FileOutputStream(guildFile)));
        } catch (Exception e) {
            Bot.errorHandler.error(message.getChannel());
            e.printStackTrace();
        }
    }

    public void readRoleMenus() {
        List<File> guildFiles = new ArrayList<>();
        Path guildPath = Paths.get(resourcesPath);

        try (Stream<Path> temp = Files.walk(guildPath, 1)) {
            List<Path> files = new ArrayList<>();
            temp.forEach(files::add);

            files.stream().filter(file ->
                    file.getFileName().toString().replace(".xml", "")
                            .matches("[0-9]{18}"))
                            .forEach(path -> guildFiles.add(new File(path.toUri())));

            for (File guildFile : guildFiles) {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(guildFile);

                Element rootEle = (Element) document.getElementsByTagName("Config").item(0);

                Element roleMenusEle = (Element) rootEle.getElementsByTagName("RoleMenus").item(0);
                NodeList roleMenus = roleMenusEle.getElementsByTagName("RoleMenu");

                for (int i = 0; i < roleMenus.getLength(); i++) {
                    if (roleMenus.item(i).getNodeType() != Node.ELEMENT_NODE) { continue; }
                    Element roleMenu = (Element) roleMenus.item(i);

                    String messageId = roleMenu.getElementsByTagName("Id").item(0).getTextContent();

                    NodeList selections = roleMenu.getElementsByTagName("Selection");
                    Map<String, Role> stringRoleList = new HashMap<>();

                    for (int s = 0; s < selections.getLength(); s++) {
                        if (selections.item(i).getNodeType() != Node.ELEMENT_NODE) { continue; }
                        Element selection = (Element) selections.item(s);

                        String emoji = selection.getElementsByTagName("Emoji").item(0).getTextContent();

                        String roleId = selection.getElementsByTagName("RoleId").item(0).getTextContent();
                        Role role = Bot.client.getRoleById(roleId);

                        stringRoleList.put(emoji, role);
                    }

                    Bot.reactionHandler.addReactionMessage(messageId, stringRoleList);
                }
            }
        } catch (Exception e) {
            Bot.logHandler.LOGGER.fatal("Error in reading role menus");
            Bot.errorHandler.printStackTrace(e);
            System.exit(1);
        }
    }

    public void removeRoleMenu(String guildId, String channelId, String messageId) {
        File guildFile = new File(getGuildPath(guildId).toUri());

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document newDocument = db.newDocument();
            Document currentDocument = db.parse(guildFile);

            Element rootEle = newDocument.createElement("Config");

            String prefix = currentDocument.getElementsByTagName("Prefix").item(0).getTextContent();
            Element prefixEle = newDocument.createElement("Prefix");
            prefixEle.appendChild(newDocument.createTextNode(prefix));
            rootEle.appendChild(prefixEle);

            NodeList roleMenus = currentDocument.getElementsByTagName("RoleMenus");
            Node roleMenusImported = newDocument.importNode(roleMenus.item(0), true);
            Element roleMenusEle = (Element) roleMenusImported;

            NodeList roleMenuNodes = roleMenusEle.getElementsByTagName("RoleMenu");

            Node targetNode = null;

            for (int i = roleMenuNodes.getLength() - 1; i >= 0; i--) {
                if (roleMenuNodes.item(i).getNodeType() != Node.ELEMENT_NODE) { return; }
                Element roleMenuEle = (Element) roleMenuNodes.item(i);

                if (roleMenuEle.getElementsByTagName("Id").item(0).getTextContent().equals(messageId)) {
                    targetNode = roleMenuNodes.item(i);
                    break;
                }
            }

            if (targetNode == null) {
                throw new Exception("Weird error that isn't supposed to happens");
            }

            roleMenusEle.removeChild(targetNode);

            rootEle.appendChild(roleMenusEle);
            newDocument.appendChild(rootEle);

            beautifyDocument(newDocument);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setParameter("indent-number", "4");

            tr.transform(new DOMSource(newDocument), new StreamResult(new FileOutputStream(guildFile)));
        } catch (Exception e) {
            Bot.errorHandler.error(Objects.requireNonNull(Bot.client.getTextChannelById(channelId)));
            Bot.errorHandler.printStackTrace(e);
        }
    }

    private void checkGuildExist(Guild guild) {
        Path guildPath = Paths.get(resourcesPath, guild.getId() + ".xml").toAbsolutePath().normalize();
        if (Files.notExists(guildPath)) {
            try {
                Files.copy(defaultConfigPath, guildPath);
            } catch (Exception e) {
                Bot.errorHandler.error(guild);
                e.printStackTrace();
            }
        }
    }

    private Path getGuildPath(String guildId) {
        return Paths.get(resourcesPath, guildId + ".xml").toAbsolutePath().normalize();
    }

    private void beautifyDocument(Document document) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                    document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }
        } catch(Exception e) {
            Bot.logHandler.LOGGER.error("Error beautifying document");
            Bot.errorHandler.printStackTrace(e);
        }
    }
}
