package com.jw2304.pointing.casual.tasks.stroop;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class StroopWebSocketHandler extends TextWebSocketHandler {
    public static final Logger LOG = LoggerFactory.getLogger(StroopWebSocketHandler.class);

    private final ScheduledExecutorService stroopScheduler = Executors.newScheduledThreadPool(4);
    private ScheduledFuture<?> stroopFuture = null;
    private WebSocketSession session = null;
    private final Random rng = new Random(System.currentTimeMillis());

    // need to get a way to generate stroop words

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        StroopDistractorController.LOG.info("Stroop WebSocket connection Started");
        this.session = session;
        session.sendMessage(new TextMessage("{\"word\": \"CONNECTED\", \"colour\": \"green\"}"));  
    }

    // @Override
    // public void handleTextMessage(WebSocketSession session, TextMessage message) { }

    public void start() {
        IntStream.rangeClosed(1, 3)
            .forEach(i -> {
                LOG.info("Scheduling stroop %d".formatted(i));
                stroopScheduler.schedule(() -> {
                    try {
                        LOG.info("Stroop %d".formatted(i));
                        this.session.sendMessage(new TextMessage("{\"word\": \"%d\", \"colour\": \"white\"}".formatted(i)));
                    } catch (IOException ioex) {
                        StroopDistractorController.LOG.error("Failed to send over stroop websocket", ioex);
                    }
                }, 3-i, TimeUnit.SECONDS);
            }
        );

        stroopFuture = stroopScheduler.schedule(() -> scheduleStroop(), 3, TimeUnit.SECONDS);
    }

    public void scheduleStroop() {
        LOG.info("Sending next Stroop word");
        try {
            String word = StroopColours.values()[rng.nextInt(StroopColours.values().length)].name();
            String colour = StroopColours.values()[rng.nextInt(StroopColours.values().length)].cssColour;
            this.session.sendMessage(new TextMessage("{\"word\": \"%s\", \"colour\": \"%s\"}".formatted(word, colour)));
        } catch (IOException ioex) {
            StroopDistractorController.LOG.error("Failed to send over stroop websocket", ioex);
        }
        stroopScheduler.schedule(() -> {
            try {
                    this.session.sendMessage(new TextMessage("{\"word\": \"\", \"colour\": \"black\"}"));
                } catch (IOException ioex) {
                    StroopDistractorController.LOG.error("Failed to send over stroop websocket", ioex);
                }
            }, 2500, TimeUnit.MILLISECONDS);
        stroopFuture = stroopScheduler.schedule(() -> scheduleStroop(), rng.nextInt(3)+3, TimeUnit.SECONDS);
    }

    public void stop() {
        if (stroopFuture != null) {
            stroopFuture.cancel(true);
        }
    }

    public enum StroopColours {
        RED("red"),
        GREEN("dark-green"),
        BLUE("dark-blue"),
        YELLOW("yellow"),
        PINK("pink"),
        ORANGE("dark-orange");

        public final String cssColour;
        StroopColours(String css) {
            cssColour = css;
        }
    }
}