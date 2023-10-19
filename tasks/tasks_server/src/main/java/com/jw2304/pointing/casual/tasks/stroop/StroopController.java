package com.jw2304.pointing.casual.tasks.stroop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.jw2304.pointing.casual.tasks.connections.WebSocketConnectionConsumer;

@Controller
public class StroopController implements WebSocketConnectionConsumer {
    public static final Logger LOG = LoggerFactory.getLogger(StroopController.class);

    private final ScheduledExecutorService stroopScheduler = Executors.newScheduledThreadPool(4);
    private ScheduledFuture<?> stroopFuture = null;
    private WebSocketSession uiWebSocket = null;
    private List<StroopPayload> sequence = new ArrayList<StroopPayload>();
    private final AtomicInteger taskSequenceIdx = new AtomicInteger(0);
    private final Random rng = new Random(System.currentTimeMillis());

    @Override
    public String getSocketRegistrationPath() {
        return "stroop";
    }

    @Override
    public void connectionRegistered(WebSocketSession session) {
        LOG.info("Stroop WebSocket connection Started");
        uiWebSocket = session;
        // try {
        //     uiWebSocket.sendMessage(new TextMessage("{\"word\": \"CONNECTED\", \"colour\": \"green\"}"));  
        // } catch (IOException ioex) {
        //     LOG.error("Unable to send connection message to the ", ioex);
        // }
    }

    public void start(List<StroopPayload> sequence) {
        this.sequence = sequence;
        
        stroopFuture = stroopScheduler.schedule(() -> {
            try {
                uiWebSocket.sendMessage(new TextMessage("{\"word\": \"\", \"colour\": \"black\"}"));
            } catch(IOException ioex) {
                LOG.error("Unable to send reset to stroop", ioex);
            }
            scheduleStroop();
        }, 3, TimeUnit.SECONDS);
    }

    public void scheduleStroop() {
        LOG.info("Sending next Stroop word");
        try {
            String word = StroopColour.values()[rng.nextInt(StroopColour.values().length)].name();
            String colour = StroopColour.values()[rng.nextInt(StroopColour.values().length)].cssColour;
            uiWebSocket.sendMessage(new TextMessage("{\"word\": \"%s\", \"colour\": \"%s\"}".formatted(word, colour)));
        } catch (IOException ioex) {
            LOG.error("Failed to send over stroop websocket", ioex);
        }
        stroopScheduler.schedule(() -> {
            try {
                uiWebSocket.sendMessage(new TextMessage("{\"word\": \"\", \"colour\": \"black\"}"));
            } catch (IOException ioex) {
                LOG.error("Failed to send over stroop websocket", ioex);
            }
        }, 2500, TimeUnit.MILLISECONDS);
        stroopFuture = stroopScheduler.schedule(() -> scheduleStroop(), rng.nextInt(3)+3, TimeUnit.SECONDS);
    }

    public void stop() {
        if (stroopFuture != null) {
            stroopFuture.cancel(true);
        }
    }

    public class StroopPayload {
        public StroopColour colour;
        public String word;
    }

    public enum StroopColour {
        RED("red"),
        GREEN("dark-green"),
        BLUE("dark-blue"),
        YELLOW("yellow"),
        PINK("pink"),
        ORANGE("dark-orange");

        public final String cssColour;
        StroopColour(String css) {
            cssColour = css;
        }
    }

}