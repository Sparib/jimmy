package org.sparib.jimmy.classes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Pingable {
    private String name;                        // Name of ping for use in embeds sent
    private PingType pingType;                          // Type of ping
    private String tcpAddress = null;           // Address for tcp
    private int tcpPort = 0;                    // Port for tcp
    private final TextChannel channel;          // Channel to send updates in
    private final int pingTime;                 // How quickly to ping (ms)
    private final EmbedBuilder successEmbed;    // Embed sent on reconnect
    private final EmbedBuilder failEmbed;       // Embed sent/updated on disconnect
    private OkHttpClient client;                // Client for http
    private Request request;                    // Request for http
    private ScheduledFuture<?> pingSchedule;    // Schedule for ping
    private boolean lastPingSuccess = true;     // If last ping was successful
    private Message errorMessage = null;        // Message that contains error embed to update error embed
    private int pingFailNumber = 0;             // Times connection failed to get (relative) time of disconnect

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            if (lastPingSuccess) {
                failEmbed.setDescription("Failed to reach host.");
                channel.sendMessage(failEmbed.build()).queue(m -> errorMessage = m);
            } else {
                pingFailNumber++;
                failEmbed.setDescription("Failed to reach host for " + getFailTime() + " seconds.");
                errorMessage.editMessage(failEmbed.build()).complete();
            }
            Bot.logHandler.LOGGER.error(lastPingSuccess);
            lastPingSuccess = false;
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
            Bot.logHandler.LOGGER.info(lastPingSuccess);
            if (response.isSuccessful()) {
                Bot.logHandler.success("Successfully pinged http");
                if (!lastPingSuccess) {
                    successEmbed.setDescription("Reconnected after " + getFailTime() + " seconds.");
                    errorMessage.editMessage(successEmbed.build()).complete();
                    pingFailNumber = 0;
                }
                lastPingSuccess = true;
            } else {
                if (lastPingSuccess) {
                    failEmbed.setDescription("Failed on code " + response.code() + ".");
                    channel.sendMessage(failEmbed.build()).queue(m -> errorMessage = m);
                } else {
                    pingFailNumber++;
                    failEmbed.setDescription(String.format("Failed on code %d for %d seconds.", response.code(), getFailTime()));
                    errorMessage.editMessage(failEmbed.build()).complete();
                }
                lastPingSuccess = false;
            }
        }
    };

    public Pingable(String name, String url, int pingTime, PingType pingType, TextChannel channel, Message message) {
        // Setting of globals
        this.name = name;
        this.pingType = pingType;
        this.channel = channel;
        this.pingTime = pingTime;

        // Creation of global embeds
        this.successEmbed = new EmbedBuilder()
                .setTitle(String.format("`%s` is back online!", this.name))
                .setColor(Color.GREEN);

        this.failEmbed = new EmbedBuilder()
                .setTitle(String.format("`%s` has failed!", this.name))
                .setColor(Color.RED);

        // Set up of actual pinging based on selected connection pingType
        if (pingType.equals(PingType.HTTP)) {
            // Builds client and request
            client = new OkHttpClient();
            request = new Request.Builder()
                    .url(url)
                    .build();
        } else if (pingType.equals(PingType.TCP)) {
            // Checks if input is url/ip
            if (!url.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})?") &&
                    !url.matches("localhost:[0-9]{1,5}")) {
                try {
                    new URL(url);
                } catch (MalformedURLException e) {
                    MessageEmbed errorEmbed = new EmbedBuilder()
                            .setTitle("Error creating pingable `" + this.name + "`")
                            .setDescription("The inputted url is not a correct url!")
                            .setColor(Color.RED)
                            .setTimestamp(Instant.now())
                            .setFooter("Command attempted by " + message.getAuthor().getAsTag())
                            .build();
                    message.getChannel().sendMessage(errorEmbed).complete();
                    return;
                }
            }

            // Split url/ip to host/port and set accordingly
            String[] urlParts = url.split(":");
            this.tcpAddress = urlParts[0];
            if (urlParts.length == 2) {
                try  {
                    this.tcpPort = Integer.parseInt(urlParts[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.tcpPort = 27015;
            }

            Bot.logHandler.LOGGER.info("Address: " + this.tcpAddress);
            Bot.logHandler.LOGGER.info("Port: " + this.tcpPort);
        }
    }

    private void ping() {
        // Creates a new request with callback
        this.client.newCall(this.request).enqueue(this.callback);
    }

    private void pingTcp() {
        // Sets up try for socket ping
        try (Socket ignored = new Socket(this.tcpAddress, this.tcpPort)) {
            // On: Connected
            Bot.logHandler.LOGGER.info("Connected tcp");
            if (!this.lastPingSuccess) {
                successEmbed.setDescription("Reconnected after " + getFailTime() + " seconds.");
                errorMessage.editMessage(successEmbed.build()).complete();
                pingFailNumber = 0;
            }
            this.lastPingSuccess = true;
        } catch (ConnectException ce) {
            // On: Didn't Connect
            Bot.logHandler.LOGGER.info("Didn't connect tcp");
            if (this.lastPingSuccess) {
                this.failEmbed.setDescription("Failed to connect.");
                Bot.logHandler.LOGGER.info("Set description");
                this.channel.sendMessage(failEmbed.build()).queue(m -> errorMessage = m);
                Bot.logHandler.LOGGER.info("Send and set error message");
            } else {
                pingFailNumber++;
                this.failEmbed.setDescription("Failed to connect for " + getFailTime() + " seconds.");
                Bot.logHandler.LOGGER.info("Updated description");
                errorMessage.editMessage(failEmbed.build()).complete();
                Bot.logHandler.LOGGER.info("Edited error message");
            }
            this.lastPingSuccess = false;
        } catch (UnknownHostException uhe) {
            // On: Unknown Host
            Bot.logHandler.LOGGER.info("Unknown host tcp");
            pingSchedule.cancel(true);
            MessageEmbed errorEmbed = new EmbedBuilder()
                    .setTitle("Error with `" + this.name + "`")
                    .setDescription("The input host is unknown!")
                    .setColor(Color.RED)
                    .setTimestamp(Instant.now())
                    .build();
            this.channel.sendMessage(errorEmbed).complete();
        } catch (Exception e) {
            // On: Other Exception
            Bot.logHandler.LOGGER.info("Other exception tcp");
            Bot.errorHandler.error(this.channel);
            Bot.errorHandler.printStackTrace(e);
        }
    }

    public void startPing() {
        Runnable pingRun = this.pingType == PingType.HTTP ? this::ping : this::pingTcp;
        // Schedules and starts ping
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        pingSchedule = executorService.scheduleAtFixedRate(
                pingRun,
                0,
                pingTime,
                TimeUnit.MILLISECONDS);
    }

    // Stops ping manually
    public void stopPing() {
        if (!pingSchedule.isCancelled()) {
            pingSchedule.cancel(true);
        }
    }

    // Calculates time disconnected based on number of failed pings and time between pings
    private int getFailTime() {
        return (this.pingTime * this.pingFailNumber) / 1000;
    }
}
