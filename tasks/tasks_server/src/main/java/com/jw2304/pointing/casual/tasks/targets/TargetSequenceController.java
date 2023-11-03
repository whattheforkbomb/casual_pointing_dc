package com.jw2304.pointing.casual.tasks.targets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.jw2304.pointing.casual.tasks.connections.WebSocketConnectionConsumer;
import com.jw2304.pointing.casual.tasks.stroop.StroopController;
import com.jw2304.pointing.casual.tasks.stroop.data.Stroop;
import com.jw2304.pointing.casual.tasks.targets.data.Target;
import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;
import com.jw2304.pointing.casual.tasks.util.Helpers;

import jakarta.annotation.PostConstruct;

@Controller
public class TargetSequenceController  implements WebSocketConnectionConsumer {

    public static Logger LOG = LoggerFactory.getLogger(TargetSequenceController.class);

    // public static final byte OFF = 0b00000000;
    public static final Target identifyColumn(int col) {
        return new Target((col*3)+1, col);
    }

    private final AtomicInteger taskSequenceIdx = new AtomicInteger(0);

    public final ScheduledExecutorService targetScheduler = Executors.newScheduledThreadPool(2);

    @Value("${targets.expected.subGroup}")
    public int subTargetCount;

    @Value("${targets.expected.linked}")
    public int targetsPerConnection;

    @Value("${targets.task.count}")
    public int taskCount;

    @Value("${targets.delay.seconds.on}")
    public int targetOnDelay;

    @Value("${targets.delay.seconds.off}")
    public int targetOffDelay;

    private TargetColour colour = TargetColour.RED;
    private TargetType targetType = TargetType.UNKNOWN;

    private List<Target> targetSequence = new ArrayList<>();

    @Autowired
    ArrayList<Socket> targetSockets;

    @Autowired
    ExecutorService executor;

    @Autowired
    StroopController stroopController;

    CommandSequenceGenerator generator;

    private WebSocketSession uiWebSocket = null;

    @Override
    public String getSocketRegistrationPath() {
        return "meta";
    }

    @Override
    public void connectionRegistered(WebSocketSession session) {
        LOG.info("meta WebSocket connection Started");
        uiWebSocket = session;
    }

    @PostConstruct
    public void setupGenerator() {
        generator = CommandSequenceGenerator.create(colour, subTargetCount, taskCount);
    }
    
    public void run(
        TargetType targetType, int targetStartDelaySeconds, int targetDurationSeconds, int stroopStartDelaySeconds, 
        int stroopDurationSeconds, boolean distractor, String participantId, String fileName, Map<Integer, Integer> socketToColumnMapping
    ) {
        File sessionSequenceFile = new File("%s.txt".formatted(fileName));
        sessionSequenceFile.getParentFile().mkdirs();

        taskSequenceIdx.set(0);
        
        int targetCount = targetSockets.size() * targetsPerConnection;
        
        Pair<List<Target>, List<Stroop>> sequences = generator.generateSequence(targetType, targetStartDelaySeconds, targetDurationSeconds, stroopStartDelaySeconds, stroopDurationSeconds, distractor, targetCount);

        targetSequence = sequences.getFirst();

        long currentTime = System.currentTimeMillis();
        IntStream.range(0, 4).forEach(i -> {
            targetScheduler.schedule(() -> {
                try {
                    uiWebSocket.sendMessage(new TextMessage("{\"count\": %d, \"progress\": -1}".formatted(i)));
                } catch (IOException ioex) {
                    LOG.error("Unable to send instructions to screen", ioex);
                }
            }, 3-i, TimeUnit.SECONDS);
        });

        long delay = 3000 - (System.currentTimeMillis() - currentTime);
        if (distractor) {
            stroopController.start(sequences.getSecond(), currentTime, fileName);
            delay += targetSequence.get(0).startDelayMilliseconds;
        }

        scheduleTargetOn(delay, sessionSequenceFile, socketToColumnMapping);
    }

    private void scheduleTargetOn(long delay, File sessionSequenceFile, Map<Integer, Integer> socketToColumnMapping) {
        int idx = taskSequenceIdx.get();
        LOG.info("Scheduling Sending Pointing Command: %d - delay: %d".formatted(idx, delay));
        targetScheduler.schedule(() -> {
            if (idx < targetSequence.size()) {
                Target target = targetSequence.get(idx);
                sendCommand(socketToColumnMapping.get(target.col), target.getCommandByte(colour, targetType));
                try {
                    uiWebSocket.sendMessage(new TextMessage("{\"count\": -1, \"progress\": -1, \"target\": %d}".formatted(target.id)));
                } catch (IOException ioex) {
                    LOG.error("Unable to send current target for visual", ioex);
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                    bw.write("%s,ON,%d,%d,%d,%d,%s\n".formatted(Helpers.getCurrentDateTimeString(), target.id,target.row, target.col, target.subTarget, Integer.toBinaryString(target.getCommandByte(colour, targetType)).substring(0, 8)));
                } catch (IOException ioex) {
                    LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                }
                LOG.info("Sending Pointing Command: %d/%d".formatted(idx+1, targetSequence.size()));
                scheduleTargetOff(target.durationMilliseconds, sessionSequenceFile, socketToColumnMapping);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void scheduleTargetOff(int duration, File sessionSequenceFile, Map<Integer, Integer> socketToColumnMapping) {
        int idx = taskSequenceIdx.getAndIncrement();
        LOG.info("Scheduling Ending Command: %d - delay: %d".formatted(idx, duration));
        targetScheduler.schedule(() -> {
            if (idx < targetSequence.size()) {
                Target target = targetSequence.get(idx);
                sendCommand(socketToColumnMapping.get(targetSequence.get(idx).col), Target.OFF);
                try {
                    LOG.info("Preparing to send progress");
                    float progress = (float) idx+1 / targetSequence.size();
                    int progressRounded = Math.round(progress * 100);
                    uiWebSocket.sendMessage(new TextMessage("{\"count\": -1, \"progress\": %d, \"target\": -1}".formatted(progressRounded)));
                } catch (IOException ioex) {
                    LOG.error("Unable to send progress update...", ioex);
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                    bw.write("%s,OFF,%d,%d,%d,%d,%s\n".formatted(Helpers.getCurrentDateTimeString(), target.id,target.row, target.col, target.subTarget, Integer.toBinaryString(target.getCommandByte(colour, targetType)).substring(0, 8)));
                } catch (IOException ioex) {
                    LOG.error("Unable to write to file: '%s'".formatted(sessionSequenceFile.getPath()), ioex);
                }
                LOG.info("Ending Pointing Command: %d/%d".formatted(idx+1, targetSequence.size()));
                scheduleTargetOn(targetSequence.get(taskSequenceIdx.get()).startDelayMilliseconds, sessionSequenceFile, socketToColumnMapping);
            }
        }, duration, TimeUnit.MILLISECONDS);
    }

    public void sendCommand(int socketId, byte command) {
        try {
            targetSockets.get(socketId).getOutputStream().write(new byte[] { command });
        } catch (IOException ioex) {
            // do something more here???
            LOG.error("Unable to send message to socket", ioex);
        }
    }

    public void resetTargets() {
        targetSockets.forEach(socket -> {
            try {
                // reset all targets
                socket.getOutputStream().write(new byte[] { Target.OFF });
            } catch (IOException ioex) {
                LOG.error("Unable to send command to socket: %s".formatted(socket.getInetAddress().getHostAddress()), ioex);
            }
        });
    }

}
