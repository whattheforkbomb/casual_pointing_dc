package com.jw2304.pointing.casual.tasks.targets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import com.jw2304.pointing.casual.tasks.stroop.data.StroopColour;
import com.jw2304.pointing.casual.tasks.targets.data.ResetTarget;
import com.jw2304.pointing.casual.tasks.targets.data.Target;
import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;
import com.jw2304.pointing.casual.tasks.util.Helpers;

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

    private File sessionSequenceFile = null;
    private String sessionSequenceFilename = null;
    private List<Target> activeTargetSequence = new ArrayList<>();
    private List<Pair<List<Target>, List<Stroop>>> stagedSequences = new ArrayList<>();

    @Autowired
    Map<String, Socket> targetSockets;

    @Autowired
    ExecutorService executor;

    @Autowired
    StroopController stroopController;

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
    
    public void countdown(int count) {
        IntStream.range(0, count).forEach(i -> {
            targetScheduler.schedule(() -> {
                try {
                    uiWebSocket.sendMessage(new TextMessage("{\"count\": %d, \"progress\": -1}".formatted(count-i)));
                } catch (IOException ioex) {
                    LOG.error("Unable to send instructions to screen", ioex);
                }
            }, i, TimeUnit.SECONDS);
        });
    }

    public void run(
        TargetType targetType, int targetStartDelayMilliSeconds, int targetDurationMilliSeconds, 
        int stroopStartDelayMilliSeconds, int stroopDurationMilliSeconds, int jitterAmount, 
        boolean distractor, boolean flash, int flashRate, String participantId, String fileName, 
        Map<Integer, String> socketToColumnMapping, Set<StroopColour> colourFilter
    ) {
        sessionSequenceFilename = fileName;
        sessionSequenceFile = new File("%s.txt".formatted(sessionSequenceFilename));
        sessionSequenceFile.getParentFile().mkdirs();
        int targetCount = socketToColumnMapping.size() * targetsPerConnection;
        
        stagedSequences = CommandSequenceGenerator.create(colour, subTargetCount, taskCount)
            .generateSequence(targetType, targetStartDelayMilliSeconds, targetDurationMilliSeconds, stroopStartDelayMilliSeconds, stroopDurationMilliSeconds, jitterAmount, distractor, targetCount, colourFilter);

        resume(flash, flashRate, socketToColumnMapping);
    }

    public void resume(boolean flash, int flashRate, Map<Integer, String> socketToColumnMapping) throws IndexOutOfBoundsException {
        if (stagedSequences.size() < 1) {
            throw new IndexOutOfBoundsException("There are no further target sequences to perform.");
        }
        Pair<List<Target>, List<Stroop>> nextSequence = stagedSequences.remove(0);
        activeTargetSequence = nextSequence.getFirst();
        taskSequenceIdx.set(0);

        int flashCountDown = flash ? flashRate : -1;
        if (!flash) flashRate = -1;

        long currentTime = System.currentTimeMillis();
        countdown(5);

        long delay = 5000 - (System.currentTimeMillis() - currentTime);
        if (nextSequence.getSecond().size() > 0) {
            stroopController.start(nextSequence.getSecond(), currentTime, sessionSequenceFilename);
            delay += activeTargetSequence.get(0).startDelayMilliseconds;
        }

        scheduleTargetOn(delay, socketToColumnMapping, flashCountDown, flashRate);
    }

    private void scheduleTargetOn(long delay, Map<Integer, String> socketToColumnMapping, int flash, int flashRate) {        
        int idx = taskSequenceIdx.get();
        if (idx < activeTargetSequence.size()) {
            LOG.info("Scheduling Sending Pointing Command: %d - delay: %d - %s".formatted(idx+1, delay, activeTargetSequence.get(idx)));
            targetScheduler.schedule(() -> {
                Target target = activeTargetSequence.get(idx);
                try {
                    boolean firstFlash = flash == flashRate;
                    if (firstFlash) {
                        try {
                            if (idx == 0) {
                                // remove the 1 on screen (from countdown), if stroop not already done so. 
                                // Ideally move this logic to UI, e.g. if target sent, then clear screen if 1 shown
                                uiWebSocket.sendMessage(new TextMessage("{\"count\": 0, \"progress\": -1}"));
                            }
                        } catch (IOException ioex) {
                            LOG.error("Unable to send current target for visual", ioex);
                        }
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                            bw.write("%s,ON,%d,%d,%d,%d,%s\n".formatted(Helpers.getCurrentDateTimeString(), target.id,target.row, target.col, target.subTarget, Integer.toBinaryString(target.getCommandByte(colour)).substring(0, 8)));
                        } catch (IOException ioex) {
                            LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                        }
                    }
                    LOG.info("Sending Pointing Command: %d/%d - %s".formatted(idx+1, activeTargetSequence.size(), target));
                    sendCommand(socketToColumnMapping, target, firstFlash);
                } catch (Exception pkmn) {
                    LOG.error("Sometimes getting program silently crashing/stopping, hoping can be caught by this...", pkmn);
                }
                scheduleTargetOff(flashRate > 0 ? target.durationMilliseconds / (2 * flashRate) : target.durationMilliseconds, socketToColumnMapping, flash, flashRate);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void scheduleTargetOff(int duration, Map<Integer, String> socketToColumnMapping, int currentFlash, int flashRate) {
        int idx = flashRate > 0 && currentFlash > 1 ? taskSequenceIdx.get() : taskSequenceIdx.getAndIncrement();
        int flash = flashRate > 0 && currentFlash <= 1 ? flashRate : currentFlash-1; // if current flash is 1, then we've turned on the correct number of times.
        if (idx < activeTargetSequence.size()) {
            LOG.info("Scheduling Ending Command: %d - delay: %d".formatted(idx+1, duration));
            targetScheduler.schedule(() -> {
                Target target = activeTargetSequence.get(idx);
                boolean lastFlash = flash == flashRate;
                try {
                    LOG.info("Ending Pointing Command: %d/%d - %s".formatted(idx+1, activeTargetSequence.size(), target));
                    double progress = lastFlash ? (idx+1) / (float) activeTargetSequence.size()*100 : -1;
                    sendCommand(socketToColumnMapping, new ResetTarget(target.col), false, progress);
                    if (lastFlash) {
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                            bw.write("%s,OFF,%d,%d,%d,%d,%s\n".formatted(Helpers.getCurrentDateTimeString(), target.id, target.row, target.col, target.subTarget, Integer.toBinaryString(new ResetTarget(target.col).getCommandByte(TargetColour.OFF))));
                        } catch (IOException ioex) {
                            LOG.error("Unable to write to file: '%s'".formatted(sessionSequenceFile.getPath()), ioex);
                        }
                    }
                } catch (Exception pkmn) {
                    LOG.error("Sometimes getting program silently crashing/stopping, hoping can be caught by this...", pkmn);
                }
                scheduleTargetOn(lastFlash ? activeTargetSequence.get(idx).startDelayMilliseconds : target.durationMilliseconds / (2 * flashRate), socketToColumnMapping, flashRate > -1 ? flash : -1, flashRate);
            }, duration, TimeUnit.MILLISECONDS);
        }
    }

    public void sendCommand(Map<Integer, String> socketToColumnMapping, Target target, boolean tone) {
        sendCommand(socketToColumnMapping, target, tone, -1);
    }

    public void sendCommand(Map<Integer, String> socketToColumnMapping, Target target, boolean tone, double progress) {
        LOG.info("Sending Tone Request: %s".formatted(Integer.toBinaryString(target.getToneCommandByte())));
        try {
            String socketId = socketToColumnMapping.get(target.col);
            sendCommand(targetSockets.get(socketId), target, tone, progress);
        } catch  (NullPointerException npex) {
            LOG.error("Unable to retrieve socket for target column: %s".formatted(target.col), npex);
        }
    }

    public void sendCommand(Socket socket, Target target, boolean tone, double progress) {
        try {
            OutputStream stream = socket.getOutputStream();
            if (tone) {
                LOG.info("Sending Tone Request: %s".formatted(Integer.toBinaryString(target.getToneCommandByte())));
                stream.write(target.getToneCommandByte());
                try {
                    uiWebSocket.sendMessage(
                        new TextMessage("{\"count\": -1, \"progress\": -1, \"target\": %d, \"subTarget\": %d}".formatted(target.id, target.subTarget))
                    );
                } catch (IOException ioex) {
                    LOG.error("Unable to send message to update diagram", ioex);
                }
            } else if (progress > -1) {
                try {
                    uiWebSocket.sendMessage(
                        new TextMessage("{\"count\": -1, \"progress\": %f, \"target\": -1, \"subTarget\": -1}".formatted(progress))
                    );
                } catch (IOException ioex) {
                    LOG.error("Unable to send progress update...", ioex);
                }
            }
            stream.write(target.getCommandByte(colour));
        } catch (IOException ioex) {
            // do something more here???
            LOG.error("Unable to send message to socket", ioex);
        } catch (NullPointerException npex) {
            LOG.error("Unable to retrieve socket from address: %s".formatted(socket.getInetAddress().getHostAddress()), npex);
        }
    }

    public void resetTargets() {
        List<Socket> sockets = new ArrayList<>(targetSockets.values());
        for (int i=0; i<sockets.size(); i++) {
            sendCommand(sockets.get(i), new ResetTarget(i), false, 100);
        }
    }

}
