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
import java.util.concurrent.ScheduledFuture;
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

    private List<Target> targetSequence = new ArrayList<>();

    @Autowired
    Map<String, Socket> targetSockets;

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
        TargetType targetType, int targetStartDelayMilliSeconds, int targetDurationMilliSeconds, 
        int stroopStartDelayMilliSeconds, int stroopDurationMilliSeconds, int jitterAmount, 
        boolean distractor, boolean flash, int flashRate, String participantId, String fileName, Map<Integer, String> socketToColumnMapping
    ) {
        File sessionSequenceFile = new File("%s.txt".formatted(fileName));
        sessionSequenceFile.getParentFile().mkdirs();

        taskSequenceIdx.set(0);
        
        int targetCount = socketToColumnMapping.size() * targetsPerConnection;
        
        Pair<List<Target>, List<Stroop>> sequences = generator.generateSequence(targetType, targetStartDelayMilliSeconds, targetDurationMilliSeconds, stroopStartDelayMilliSeconds, stroopDurationMilliSeconds, jitterAmount, distractor, targetCount);

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

        int flashCountDown = flash ? flashRate : -1;
        if (!flash) flashRate = -1;
        LOG.info("Flash: %b, Flash Countdown: %d, Flash Rate: %d".formatted(flash, flashCountDown, flashRate));

        scheduleTargetOn(delay, sessionSequenceFile, socketToColumnMapping, flashCountDown, flashRate);
    }

    private void scheduleTargetOn(long delay, File sessionSequenceFile, Map<Integer, String> socketToColumnMapping, int currentFlash, int flashRate) {
        int flash = flashRate > 0 && currentFlash < 1 ? flashRate : currentFlash;
        
        int idx = taskSequenceIdx.get();
        if (idx < targetSequence.size()) {
            LOG.info("Scheduling Sending Pointing Command: %d - delay: %d - %s".formatted(idx+1, delay, targetSequence.get(idx)));
            targetScheduler.schedule(() -> {
                Target target = targetSequence.get(idx);
                try {
                    byte cmd = target.getCommandByte(colour);
                    if (flash == flashRate) {
                        LOG.info("Sending Tone Request: %s".formatted(Integer.toBinaryString(target.getToneCommandByte())));
                        sendCommand(socketToColumnMapping.get(target.col), target.getToneCommandByte());
                        try {
                            uiWebSocket.sendMessage(new TextMessage("{\"count\": -1, \"progress\": -1, \"target\": %d, \"subTarget\": %d}".formatted(target.id, target.subTarget)));
                        } catch (IOException ioex) {
                            LOG.error("Unable to send current target for visual", ioex);
                        }
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                            bw.write("%s,ON,%d,%d,%d,%d,%s\n".formatted(Helpers.getCurrentDateTimeString(), target.id,target.row, target.col, target.subTarget, Integer.toBinaryString(cmd).substring(0, 8)));
                        } catch (IOException ioex) {
                            LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                        }
                    }
                    LOG.info("Sending Pointing Command: %d/%d - %s".formatted(idx+1, targetSequence.size(), target));
                    sendCommand(socketToColumnMapping.get(target.col), cmd);
                } catch (Exception pkmn) {
                    LOG.error("Sometimes getting program silently crashing/stopping, hoping can be caught by this...", pkmn);
                }
                scheduleTargetOff(flashRate > 0 ? target.durationMilliseconds / (2 * flashRate) : target.durationMilliseconds, sessionSequenceFile, socketToColumnMapping, flash, flashRate);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void scheduleTargetOff(int duration, File sessionSequenceFile, Map<Integer, String> socketToColumnMapping, int flash, int flashRate) {
        int idx = flash > 0 ? taskSequenceIdx.get() : taskSequenceIdx.getAndIncrement();
        if (idx < targetSequence.size()) {
            LOG.info("Scheduling Ending Command: %d - delay: %d".formatted(idx+1, duration));
            targetScheduler.schedule(() -> {
                Target target = targetSequence.get(idx);
                try {
                    LOG.info("Ending Pointing Command: %d/%d - %s".formatted(idx+1, targetSequence.size(), target));
                    sendCommand(socketToColumnMapping.get(targetSequence.get(idx).col), Target.OFF);
                    if (flashRate == -1 || flash == 1) {
                        try {
                            float progress = (idx+1) / (float) targetSequence.size()*100;
                            LOG.info("Preparing to send progress %f/100: %s".formatted(progress, target));
                            uiWebSocket.sendMessage(new TextMessage("{\"count\": -1, \"progress\": %d, \"target\": -1, \"subTarget\": -1}".formatted(Math.round(progress))));
                        } catch (IOException ioex) {
                            LOG.error("Unable to send progress update...", ioex);
                        }
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                            bw.write("%s,OFF,%d,%d,%d,%d,%s\n".formatted(Helpers.getCurrentDateTimeString(), target.id, target.row, target.col, target.subTarget, Integer.toBinaryString(Target.OFF)));
                        } catch (IOException ioex) {
                            LOG.error("Unable to write to file: '%s'".formatted(sessionSequenceFile.getPath()), ioex);
                        }
                    }
                } catch (Exception pkmn) {
                    LOG.error("Sometimes getting program silently crashing/stopping, hoping can be caught by this...", pkmn);
                }
                scheduleTargetOn(flashRate > -1 && flash > 0 ? target.durationMilliseconds / (2 * flashRate) : targetSequence.get(taskSequenceIdx.get()).startDelayMilliseconds, sessionSequenceFile, socketToColumnMapping, flashRate > -1 ? flash-1 : -1, flashRate);
            }, duration, TimeUnit.MILLISECONDS);
        }
    }

    public void sendCommand(String socketId, byte command) {
        try {
            targetSockets.get(socketId).getOutputStream().write(new byte[] { command });
        } catch (IOException ioex) {
            // do something more here???
            LOG.error("Unable to send message to socket", ioex);
        } catch (NullPointerException npex) {
            LOG.error("Unable to retrieve socket from address: %s".formatted(socketId), npex);
        }
    }

    public void resetTargets() {
        targetSockets.values().forEach(socket -> {
            try {
                // reset all targets
                socket.getOutputStream().write(new byte[] { Target.OFF });
            } catch (IOException ioex) {
                LOG.error("Unable to send command to socket: %s".formatted(socket.getInetAddress().getHostAddress()), ioex);
            }
        });
    }

}
