package org.sparib.jimmy.classes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.sparib.jimmy.main.Bot;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Pingable {
    private String tcpAddress = null;
    private int tcpPort = 0;
    private final TextChannel channel;
    private final int pingTime;
    private final EmbedBuilder successEmbed;
    private final EmbedBuilder failEmbed;
    private OkHttpClient client;
    private Request request;
    private ScheduledFuture<?> pingSchedule;
    private boolean lastPingSuccess = true;
    private Message errorMessage = null;
    private int pingFailNumber = 0;

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            if (lastPingSuccess) {
                failEmbed.setDescription("Failed to reach host");
                channel.sendMessage(failEmbed.build()).queue(m -> errorMessage = m);
            } else {
                pingFailNumber++;
                int pingFailTime = (pingFailNumber * pingTime) / 1000;
                failEmbed.setDescription("Failed to reach host for " + pingFailTime + " seconds");
                errorMessage.editMessage(failEmbed.build()).complete();
            }
            Bot.logHandler.LOGGER.error(lastPingSuccess);
            lastPingSuccess = false;
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
            if (response.isSuccessful()) {
                Bot.logHandler.success("Successfully pinged http");
                if (!lastPingSuccess) {
                    errorMessage.editMessage(successEmbed.build()).complete();
                }
                lastPingSuccess = true;
            } else {
                if (lastPingSuccess) {
                    failEmbed.setDescription("Failed on code " + response.code());
                    channel.sendMessage(failEmbed.build()).queue(m -> errorMessage = m);
                } else {
                    pingFailNumber++;
                    int pingFailTime = (pingFailNumber * pingTime) / 1000;
                    failEmbed.setDescription(String.format("Failed on code %d for %d seconds", response.code(), pingFailTime));
                    errorMessage.editMessage(failEmbed.build()).complete();
                }
                lastPingSuccess = false;
            }
            Bot.logHandler.LOGGER.error(lastPingSuccess);
        }
    };

    public Pingable(String name, String url, int pingTime, Type type, TextChannel channel) {
        this.channel = channel;
        this.pingTime = pingTime;

        this.successEmbed = new EmbedBuilder()
                .setTitle(String.format("`%s` is back online!", name))
                .setColor(Color.GREEN);

        this.failEmbed = new EmbedBuilder()
                .setTitle(String.format("`%s` has failed!", name))
                .setColor(Color.RED);

        if (type.equals(Type.HTTP)) {
            client = new OkHttpClient();
            request = new Request.Builder()
                    .url(url)
                    .build();

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            pingSchedule = executorService.scheduleAtFixedRate(
                    this::ping,
                    0,
                    pingTime,
                    TimeUnit.MILLISECONDS);
        } else {
            if (!url.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})?")) { /* do error */ return; }
            String[] urlParts = url.split(":");
            if (urlParts.length == 2) {
                this.tcpAddress = urlParts[0];
                try  {
                    int port = Integer.parseInt(urlParts[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            pingSchedule = executorService.scheduleAtFixedRate(
                    this::pingTcp,
                    0,
                    pingTime,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    private void ping() {
        client.newCall(request).enqueue(callback);
    }

    private void pingTcp() {
        try (Socket socket = new Socket()) {

        } catch (Exception e) {
            Bot.errorHandler.error(this.channel);
            Bot.errorHandler.printStackTrace(e);
        }
    }

    public void stopPing() {
        if (!pingSchedule.isCancelled()) {
            pingSchedule.cancel(false);
        }
    }

    private void handleFailPing() {

    }

    public enum Type {
        TCP,
        HTTP
    }
}
