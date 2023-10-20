package com.jw2304.pointing.casual.tasks.targets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
import com.jw2304.pointing.casual.tasks.targets.data.TargetArray;
import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetLED;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;

import jakarta.annotation.PostConstruct;

@Controller
public class TargetSequenceController  implements WebSocketConnectionConsumer {

    public static Logger LOG = LoggerFactory.getLogger(TargetSequenceController.class);

    public static final byte OFF = 0b00000000;
    public static final byte IDENTIFY = 0b00111010;

    private final Random rng = new Random(System.currentTimeMillis());

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

    private List<Target> commandSequence = new ArrayList<>();

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
        return "count";
    }

    @Override
    public void connectionRegistered(WebSocketSession session) {
        LOG.info("Countdown WebSocket connection Started");
        uiWebSocket = session;
    }

    @PostConstruct
    public void setupGenerator() {
        generator = CommandSequenceGenerator.create(colour, subTargetCount, targetsPerConnection, taskCount);
    }


    // The command sequence is setup as pairs of bytes, the first being the target, the second being the command
    private void generate(TargetType targetType2, boolean distractor, HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping, TargetColour colour, String participantId) {
        // byte[] cmdSequence = new byte[taskCount*2];
        

        // if whole target, we will repeat.
        // if single LED, we will omit the same number of LED from each target.
        
        // Need to uniformly distribute across the columns and rows (e.g. next target should be different column and row; for individual LED need different row column within target, but can reuse target, should not be the same row or column on different target).

        

        // Need to save to file which target is scheduled...
        
        // for (int i=0; i<taskCount; i++) {
        //     Target nextTarget = possibleTargets.remove(rng.nextInt(possibleTargets.size()));
           
        //     commandSequence.add(i, nextTarget);

        //     try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
        //         bw.write("%d,%d,%d,%d,%s\n".formatted(nextTarget.id,nextTarget.row, nextTarget.col, nextTarget.subTarget, Integer.toBinaryString(nextTarget.getCommandByte(colour, targetType)).substring(0, 8)));
        //     } catch (IOException ioex) {
        //         LOG.error("Unable to write to file: '/home/whiff/data/%s.txt'".formatted(participantId), ioex);
        //     }
        // }
    }

    private void getNext(Target last, List<Target> remaining, boolean subTargets) {
        // filter out similar targets (same row and column)
        if (last != null) {
            if (subTargets) {
                // filter out targets
            } else {
                // remaining.stream().filter()
            }
        }

        // find next target from remaining
        rng.nextInt(remaining.size());
    } 

    

    public void run(
        TargetType targetType, int targetStartDelaySeconds, int targetDurationSeconds, int stroopStartDelaySeconds, 
        int stroopDurationSeconds, boolean distractor, String participantId
    ) {
        File sessionSequenceFile = new File("/home/whiff/data/%s/%s_%s_%s.txt".formatted(participantId, getCurrentDateTimeString(), targetType.name(), distractor ? "Distracted": "Focussed"));
        sessionSequenceFile.getParentFile().mkdirs();

        taskSequenceIdx.set(0);
        
        int targetCount = targetSockets.size() * targetsPerConnection;
        
        Pair<List<Target>, List<Stroop>> sequences = generator.generateSequence(targetType, targetStartDelaySeconds, targetDurationSeconds, stroopStartDelaySeconds, stroopDurationSeconds, distractor, targetCount);

        long currentTime = System.currentTimeMillis();
        IntStream.range(1, 4).forEach(i -> {
            targetScheduler.schedule(() -> {
                try {
                    uiWebSocket.sendMessage(new TextMessage("{\"count\": %d}".formatted(i)));
                } catch (IOException ioex) {
                    LOG.error("Unable to send instructions to screen", ioex);
                }
            }, 3-i, TimeUnit.SECONDS);
        });

        if (distractor) {
            stroopController.start(sequences.getSecond(), currentTime);
        }

        scheduleTargetOn(3000 - (System.currentTimeMillis() - currentTime), sessionSequenceFile);
    }

    private String getCurrentDateTimeString() {
        return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now(ZoneId.of("UTC"))).replace(":", ".").substring(0, 25);
    }

    private void scheduleTargetOn(long delay, File sessionSequenceFile) {
        int idx = taskSequenceIdx.get();
        LOG.info("Scheduling Sending Pointing Command: %d".formatted(idx));
        targetScheduler.schedule(() -> {
            if (idx < commandSequence.size()) {
                Target target = commandSequence.get(idx);
                sendCommand(target.col, target.getCommandByte(colour, targetType));
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                    bw.write("%s,ON,%d,%d,%d,%d,%s\n".formatted(getCurrentDateTimeString(), target.id,target.row, target.col, target.subTarget, Integer.toBinaryString(target.getCommandByte(colour, targetType)).substring(0, 8)));
                } catch (IOException ioex) {
                    LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                }
                LOG.info("Sending Pointing Command: %d/%d".formatted(idx+1, commandSequence.size()));
                scheduleTargetOff(target.durationMilliseconds, sessionSequenceFile);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void scheduleTargetOff(int duration, File sessionSequenceFile) {
        int idx = taskSequenceIdx.getAndIncrement();
        LOG.info("Scheduling Ending Command: %d".formatted(idx));
        targetScheduler.schedule(() -> {
            if (idx < commandSequence.size()) {
                Target target = commandSequence.get(idx);
                sendCommand(commandSequence.get(idx).col, OFF);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                    bw.write("%s,OFF,%d,%d,%d,%d,%s\n".formatted(getCurrentDateTimeString(), target.id,target.row, target.col, target.subTarget, Integer.toBinaryString(target.getCommandByte(colour, targetType)).substring(0, 8)));
                } catch (IOException ioex) {
                    LOG.error("Unable to write to file: '/home/whiff/data/%s'".formatted(sessionSequenceFile.getName()), ioex);
                }
                LOG.info("Ending Pointing Command: %d/%d".formatted(idx+1, commandSequence.size()));
                scheduleTargetOn(commandSequence.get(taskSequenceIdx.get()).startDelayMilliseconds, sessionSequenceFile);
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
                socket.getOutputStream().write(new byte[] { OFF });
            } catch (IOException ioex) {
                LOG.error("Unable to send command to socket: %s".formatted(socket.getInetAddress().getHostAddress()), ioex);
            }
        });
    }

}
