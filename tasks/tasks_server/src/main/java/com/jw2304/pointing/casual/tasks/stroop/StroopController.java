package com.jw2304.pointing.casual.tasks.stroop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;

import com.jw2304.pointing.casual.tasks.connections.WebSocketConnectionConsumer;
import com.jw2304.pointing.casual.tasks.util.Helpers;
import com.jw2304.pointing.casual.tasks.stroop.data.Stroop;

@Controller
public class StroopController implements WebSocketConnectionConsumer {
    public static final Logger LOG = LoggerFactory.getLogger(StroopController.class);

    private final ScheduledExecutorService stroopScheduler = Executors.newScheduledThreadPool(4);
    private ScheduledFuture<?> stroopFuture = null;
    private WebSocketSession uiWebSocket = null;
    private List<Stroop> sequence = new ArrayList<Stroop>();
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

    public void start(List<Stroop> sequence, long elapsed, String fileName) {
        this.sequence = sequence;
        taskSequenceIdx.set(0);
        File sessionSequenceFile = new File("%s_Stroop.txt".formatted(fileName));
        sessionSequenceFile.getParentFile().mkdirs();

        scheduleStroopShow(5000 - (System.currentTimeMillis() - elapsed), sessionSequenceFile);

        // stroopScheduler.schedule(() -> {
        //     try {
        //         uiWebSocket.sendMessage(Stroop.getResetMessage());
        //     } catch(IOException ioex) {
        //         LOG.error("Unable to send reset to stroop", ioex);
        //     }
        //     scheduleStroopShow(0, sessionSequenceFile);
        // }, 3000 - (System.currentTimeMillis() - elapsed), TimeUnit.MILLISECONDS);
    }

    private void scheduleStroopShow(long delay, File sessionSequenceFile) {
        int idx = taskSequenceIdx.getAndIncrement();
        LOG.info("Scheduling Sending Stroop: %d - delay: %d".formatted(idx, delay));
        stroopScheduler.schedule(() -> {
            if (idx < sequence.size()) {
                Stroop stroopPayload = sequence.get(idx);
                try {
                    uiWebSocket.sendMessage(stroopPayload.getMessage());
                } catch (IOException ioex) {
                    LOG.error("Failed to send over stroop websocket", ioex);
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                    bw.write("%s,SHOW,%s,%s\n".formatted(Helpers.getCurrentDateTimeString(), stroopPayload.word, stroopPayload.colour.name()));
                } catch (IOException ioex) {
                    LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                }
                LOG.info("Sending Stroop: %d/%d".formatted(idx+1, sequence.size()));
                scheduleStroopClear(idx, stroopPayload.durationMilliseconds, sessionSequenceFile);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void scheduleStroopClear(int idx, int duration, File sessionSequenceFile) {
        LOG.info("Scheduling Clearing Stroop: %d - delay: %d".formatted(idx, duration));
        stroopScheduler.schedule(() -> {
            if (idx < sequence.size()) {
                Stroop stroopPayload = sequence.get(idx);
                try {
                    uiWebSocket.sendMessage(Stroop.getResetMessage());
                } catch (IOException ioex) {
                    LOG.error("Failed to send over stroop websocket", ioex);
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                    bw.write("%s,CLEAR,%s,%s\n".formatted(Helpers.getCurrentDateTimeString(), stroopPayload.word, stroopPayload.colour.name()));
                } catch (IOException ioex) {
                    LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                }
                LOG.info("Clearing Stroop: %d/%d".formatted(idx+1, sequence.size()));
                scheduleStroopShow(sequence.get(taskSequenceIdx.get()).startDelayMilliseconds, sessionSequenceFile);
            }
        }, duration, TimeUnit.MILLISECONDS);
    }

    public void scheduleStroop() {
        LOG.info("Sending next Stroop word");
        try {
            Stroop stroopPayload = sequence.get(taskSequenceIdx.get());
            uiWebSocket.sendMessage(stroopPayload.getMessage());
        } catch (IOException ioex) {
            LOG.error("Failed to send over stroop websocket", ioex);
        }
        stroopScheduler.schedule(() -> {
            try {
                uiWebSocket.sendMessage(Stroop.getResetMessage());
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

}