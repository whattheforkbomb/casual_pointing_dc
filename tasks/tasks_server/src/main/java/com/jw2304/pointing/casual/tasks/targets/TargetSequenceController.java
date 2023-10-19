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
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.jw2304.pointing.casual.tasks.connections.WebSocketConnectionConsumer;
import com.jw2304.pointing.casual.tasks.stroop.StroopController;
import com.jw2304.pointing.casual.tasks.stroop.StroopController.StroopPayload;

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

    private WebSocketSession uiWebSocket = null;

    @Override
    public String getSocketRegistrationPath() {
        return "count";
    }

    @Override
    public void connectionRegistered(WebSocketSession session) {
        LOG.info("Countdown WebSocket connection Started");
        uiWebSocket = session;
        // try {
        //     uiWebSocket.sendMessage(new TextMessage("{\"count\": \"CONNECTED\"}"));  
        // } catch (IOException ioex) {
        //     LOG.error("Unable to send connection message to the ", ioex);
        // }
    }

    // The command sequence is setup as pairs of bytes, the first being the target, the second being the command
    private void generate(String targetTypeStr, boolean distractor, HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping, TargetColour colour, String participantId) {
        // byte[] cmdSequence = new byte[taskCount*2];
        commandSequence = new ArrayList<>(taskCount);
        
        int targetCount = targetSockets.size() * targetsPerConnection;
        int totalSubTargetCount = targetCount * subTargetCount;
        try {
            targetType = TargetType.valueOf(targetTypeStr.toUpperCase());
        } catch (IllegalArgumentException iaex) {
            LOG.error("Unable to parse provided targetType: %s, accepted values are {'CLUSTER', 'INDIVIDUAL'}".formatted(targetTypeStr), iaex);
            throw iaex;
        }
        this.colour = colour;

        // if whole target, we will repeat.
        // if single LED, we will omit the same number of LED from each target.
        
        // Need to uniformly distribute across the columns and rows (e.g. next target should be different column and row; for individual LED need different row column within target, but can reuse target, should not be the same row or column on different target).

        List<Target> possibleTargets = new ArrayList<>(taskCount);
        if (targetType == TargetType.CLUSTER) {
            LOG.info("Generating cluster target sequence.");
            // calc number of repeats
            // this sucks
            int targetRepeats = getRepeats(targetCount);
            for (int i=0; i<targetCount; i++) {
                for (int j=0; j<targetRepeats; j++) {
                    possibleTargets.add(new Target(i, 0));
                }
            }
        } else {
            LOG.info("Generating individual LED target sequence.");
            int targetRepeats = getRepeats(totalSubTargetCount);
            for (int i=0; i<targetCount; i++) {
                for (int j=0; j<subTargetCount; j++) {
                    for (int k=0; k<(targetRepeats < 0 ? 1 : targetRepeats); k++) {
                        possibleTargets.add(new Target(i, j));
                    }
                }
            }
            // Want to ensure same minimum number of LEDs are omitted from each cluster.
            if (targetRepeats < 0) {
                int omittedLEDs = -targetRepeats;
                int omittedPerTarget = omittedLEDs / targetCount;
                int remainingOmissions = omittedLEDs % targetCount;

                Set<Target> removed = new HashSet<>(omittedPerTarget*targetCount);

                List<Target> filteredPossibleTargets = List.copyOf(possibleTargets);
                IntStream.range(0, targetCount).forEach(i -> {
                    List<Target> filteredTargets = new ArrayList<>(filteredPossibleTargets.stream().filter(target -> target.id == i).toList());
                    for (int j=0; j<omittedPerTarget; j++) {
                        removed.add(filteredTargets.remove(rng.nextInt(filteredTargets.size())));
                    }
                });
                // remove omitted options to ensure equal number of omissions per cluster.
                possibleTargets = new ArrayList<>(possibleTargets.stream().filter(target -> removed.contains(target)).toList());
                
                // remove remaining omissions at random
                for (int i=0; i<remainingOmissions; i++) {
                    possibleTargets.remove(rng.nextInt(possibleTargets.size()));
                }
            } else {
                // we need to repeat some LEDs.
                // IGNORE FOR NOW
            }
        }

        // Need to save to file which target is scheduled...
        File sessionSequenceFile = new File("/home/whiff/data/%s/%s_%s_%s.txt".formatted(participantId, DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now(ZoneId.of("UTC"))).replace(":", "-"), targetTypeStr, distractor ? "Distracted": "Focussed"));
        sessionSequenceFile.getParentFile().mkdirs();
        for (int i=0; i<taskCount; i++) {
            Target nextTarget = possibleTargets.remove(rng.nextInt(possibleTargets.size()));
           
            commandSequence.add(i, nextTarget);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(sessionSequenceFile, true))) {
                bw.write("%d,%d,%d,%d,%s\n".formatted(nextTarget.id,nextTarget.row, nextTarget.col, nextTarget.subTarget, Integer.toBinaryString(nextTarget.getCommandByte(colour, targetType)).substring(0, 8)));
            } catch (IOException ioex) {
                LOG.error("Unable to write to file: '/home/whiff/data/%s.txt'".formatted(participantId), ioex);
            }
        }
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

    private int getRepeats(int targetCount) {
        int repeats = taskCount / targetCount;

        // there are more possible targets than tasks we want to perform.
        if (repeats == 0) {
            // return how many we want to omit
            repeats = -(targetCount % taskCount);
        } else if (taskCount % targetCount > 0) {
            repeats++;
        }

        return repeats;
    }

    public void run(String targetSize, boolean distractor, HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping, TargetColour colour, String participantId) {
        taskSequenceIdx.set(0);
        generate(targetSize, distractor, targetConnectionToPhysicalColumnMapping, colour, participantId);

        long currentTime = System.currentTimeMillis();
        // targetScheduler.execute(() -> {
        //     LOG.info("3...");
        //     for (int i=0; i<targetSockets.size(); i++) {
        //         sendCommand(i, IDENTIFY);
        //     }
        // });
        // targetScheduler.schedule(() -> {
        //     LOG.info("2...");
        //     for (int i=0; i<targetSockets.size(); i++) {
        //         sendCommand(i, (byte)0b00100000);
        //     }
        // }, 1000 - (System.currentTimeMillis() - currentTime), TimeUnit.MILLISECONDS);
        // targetScheduler.schedule(() -> {
        //     LOG.info("1...");
        //     for (int i=0; i<targetSockets.size(); i++) {
        //         sendCommand(i, (byte)0b00010000);
        //     }
        // }, 2000 - (System.currentTimeMillis() - currentTime), TimeUnit.MILLISECONDS);
        // targetScheduler.schedule(() -> {
        //     for (int i=0; i<targetSockets.size(); i++) {
        //         LOG.info("Go (Reset)");
        //         sendCommand(i, (byte)0b00000000);
        //     }
        // }, 3000 - (System.currentTimeMillis() - currentTime), TimeUnit.MILLISECONDS);

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
            stroopController.start(new ArrayList<StroopPayload>());
        }

        scheduleTargetOn(3000 - (System.currentTimeMillis() - currentTime));
    }

    private void scheduleTargetOn(long delay) {
        int idx = taskSequenceIdx.get();
        LOG.info("Scheduling Sending Pointing Command: %d".formatted(idx));
        targetScheduler.schedule(() -> {
            if (idx < commandSequence.size()) {
                Target target = commandSequence.get(idx);
                sendCommand(target.col, target.getCommandByte(colour, targetType));
                LOG.info("Sending Pointing Command: %d/%d".formatted(idx, commandSequence.size()));
                scheduleTargetOff();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void scheduleTargetOff() {
        int idx = taskSequenceIdx.getAndIncrement();
        LOG.info("Scheduling Ending Command: %d".formatted(idx));
        targetScheduler.schedule(() -> {
            if (idx < commandSequence.size()) {
                sendCommand(commandSequence.get(idx).col, OFF);
                LOG.info("Ending Pointing Command: %d/%d".formatted(idx, commandSequence.size()));
                scheduleTargetOn(TimeUnit.SECONDS.toMillis(targetOnDelay));
            }
        }, TimeUnit.SECONDS.toMillis(targetOffDelay), TimeUnit.MILLISECONDS);
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

    public class Target {
        public final int id;
        public final int subTarget;
        public final int col;
        public final int row;

        public Target(int id, int subTarget) {
            this.id = id;
            this.subTarget = subTarget;
            col = id / 3;
            row = id % 3;
        }
        
        public byte getCommandByte(TargetColour colour, TargetType targetType) {
            TargetArray array = TargetArray.values()[row];

            TargetLED led = targetType == TargetType.CLUSTER ? TargetLED.ALL : TargetLED.values()[subTarget+1];

            return (byte) (colour.mask | array.mask | led.mask);
        }
    }

    private enum TargetType {
        CLUSTER,
        INDIVIDUAL,
        UNKNOWN
    }

    // control bitmasks:
    // Colour  | Array  | LED
    // Ideally manager app will just need to send this byte to identify the target appearance

    /*  
     *  Colour:
     *  00|XX|XXXX  => Single LED (White?)
     *  01|XX|XXXX  => Green target, rest red
     *  10|XX|XXXX  => Red target, rest blue
     *  11|XX|XXXX  => Blue target, rest green
     */
    public enum TargetColour {
        WHITE(0b00000000),
        GREEN(0b01000000),
        RED(0b10000000),
        BLUE(0b11000000);
    
        public final byte mask;
        TargetColour(int mask) {
            this.mask = (byte)mask;
        }
    }
    /* 
     *  Array:
     *  XX|00|XXXX => Top
     *  XX|01|XXXX => Mid
     *  XX|10|XXXX => Bottom
     *  XX|11|XXXX => All
     */
    private enum TargetArray {
        TOP(0b00000000),
        MIDDLE(0b00010000),
        BOTTOM(0b00100000),
        ALL(0b00110000);

        public final byte mask;
        TargetArray(int mask) {
            this.mask = (byte)mask;
        }
    }

    /*
     *  Placement: Can compress location into 4 bits
     *  00|00|0000  => RESET
     *  XX|XX|0001  => Top Left
     *  XX|XX|0010  => Top Centre
     *  XX|XX|0011  => Top Right
     *  XX|XX|0100  => Mid Left
     *  XX|XX|0101  => Mid Centre
     *  XX|XX|0110  => Mid Right
     *  XX|XX|0111  => Bottom Left
     *  XX|XX|1000  => Bottom Centre
     *  XX|XX|1001  => Bottom Right
     *  XX|XX|1010  => ALL
     */
    private enum TargetLED {
        RESET(0b00000000),
        TOP_LEFT(0b00000001),
        TOP_CENTRE(0b00000010),
        TOP_RIGHT(0b00000011),
        MIDDLE_LEFT(0b00000100),
        MIDDLE_CENTRE(0b00000101),
        MIDDLE_RIGHT(0b00000110),
        BOTTOM_LEFT(0b00000111),
        BOTTOM_CENTRE(0b00001000),
        BOTTOM_RIGHT(0b00001001),
        ALL(0b00001010);

        public final byte mask;
        TargetLED(int mask) {
            this.mask = (byte)mask;
        }
    }

}
