package com.jw2304.pointing.casual.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jw2304.pointing.casual.tasks.stroop.StroopController;
import com.jw2304.pointing.casual.tasks.stroop.data.StroopColour;
import com.jw2304.pointing.casual.tasks.stroop.data.Stroop;
import com.jw2304.pointing.casual.tasks.targets.TargetSequenceController;
import com.jw2304.pointing.casual.tasks.targets.data.ResetTarget;
import com.jw2304.pointing.casual.tasks.targets.data.Target;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;
import com.jw2304.pointing.casual.tasks.util.Helpers;

@RestController
@RequestMapping(value = "/study")
public class StudyRestController {

    public static Logger LOG = LoggerFactory.getLogger(StudyRestController.class);
    public static final String UNKNOWN_PID = "UNKNOWN";
    public static final String ACCURATE_BEHAVIOUR = "ACCURATE";
    public static final String CASUAL_BEHAVIOUR = "CASUAL";
    public static final List<String> CONDITION_A = List.of(ACCURATE_BEHAVIOUR, CASUAL_BEHAVIOUR);
    public static final List<String> CONDITION_B = List.of(CASUAL_BEHAVIOUR, ACCURATE_BEHAVIOUR);

    // Session State
    private String PID = UNKNOWN_PID;
    private List<String> conditionOrder = Collections.emptyList();
    public Map<Integer, String> targetConnectionToPhysicalColumnMapping = new HashMap<Integer, String>();
    // public AtomicInteger targetColour = new AtomicInteger(2);

    @Autowired
    Map<String, Socket> targetSockets;
    
    @Autowired
    List<String> targetSocketIds;

    @Value("${data.filepath}")
    String rootFilePath;

    @Value("${targets.expected.subGroup}")
    public int subTargetCount;

    @Autowired
    TargetSequenceController targetSequenceController;

    @Autowired
    StroopController stroopHandler;

    // needed? might be fine to just loop over the 5 target columns?
    public final ScheduledExecutorService targetScheduler = Executors.newScheduledThreadPool(2);

    @GetMapping("/pid")
    public String getPID() {
        return "{ \"PID\": \"%s\", \"condition\": %s }".formatted(
            PID, 
            conditionOrder.stream()
                .collect(Collectors.joining("\", \"", "[\"", "\"]"))
        );
    }

    @PostMapping("/pid/{participantIdx}")
    public String generatePID(@PathVariable(name = "participantIdx") int participantIdx) {
        // From PIdx, generate the conditions order using latin square?
        // basically whether they start with casual or accurate pointing.
        if (participantIdx % 2 == 0) {
            conditionOrder = CONDITION_A;
        } else {
            conditionOrder = CONDITION_B;
        }

        PID = UUID.randomUUID().toString();
        return "{ \"PID\": \"%s\", \"condition\": %s }".formatted(
            PID, 
            conditionOrder.stream()
                .collect(Collectors.joining("\", \"", "[\"", "\"]"))
        );
    }

    @PostMapping("/start")
    public void start(
        @RequestParam(name = "pointingBehaviour", defaultValue = "ACCURATE") String pointingBehaviour, 
        // @RequestParam(name = "targetType", defaultValue = "INDIVIDUAL") String targetTypeStr, 
        @RequestParam(name = "flash", defaultValue = "true") boolean flash, 
        @RequestParam(name = "flashRate", defaultValue = "5") int flashRate, 
        @RequestParam(name = "targetDelay", defaultValue = "2000") int targetDelay, 
        @RequestParam(name = "targetDuration", defaultValue = "3000") int targetDuration, 
        @RequestParam(name = "jitter", defaultValue = "250") int jitterAmount
    ) {
        LOG.info("Resetting targets");
        targetSequenceController.resetTargets();

        if (UNKNOWN_PID.equals(PID)) {
            stroopHandler.sendStroop(new Stroop("Missing PID", StroopColour.RED, 0, 0));
            throw new IllegalStateException("PID has not been generated...");
        }

        LOG.info("Checking map has been updated: %d".formatted(targetConnectionToPhysicalColumnMapping.size()));

        if (targetConnectionToPhysicalColumnMapping.isEmpty()) {
            throw new IllegalStateException("There are no targets presently connected to the system");
        }

        TargetType targetType = TargetType.INDIVIDUAL;
        // TargetType targetType;
        // try {
        //     targetType = TargetType.valueOf(targetTypeStr.toUpperCase());
        // } catch (IllegalArgumentException iaex) {
        //     LOG.error("Unable to parse provided targetType: %s, accepted values are {'CLUSTER', 'INDIVIDUAL'}\nUsing INDIVIDUAL as Target Type.".formatted(targetTypeStr), iaex);
        //     targetType = TargetType.INDIVIDUAL;
        // }
        File file = new File("%s/%s".formatted(rootFilePath, PID));
        file.mkdirs();
        String sessionFileName = "%s/%s/%s_%s_%s_%s".formatted(rootFilePath, PID, pointingBehaviour, targetType.name(), "FOCUSSED", Helpers.getCurrentDateTimeString());
        targetSequenceController.run(targetType, targetDelay, targetDuration, 0, 0, jitterAmount, false, flash, flashRate, PID, sessionFileName, targetConnectionToPhysicalColumnMapping, Collections.emptySet());
    }

    @PostMapping("/start/stroop")
    public void startStroop(
        @RequestParam(name = "pointingBehaviour", defaultValue = "ACCURATE") String pointingBehaviour, 
        // @RequestParam(name = "targetType", defaultValue = "INDIVIDUAL") String targetTypeStr, 
        @RequestParam(name = "flash", defaultValue = "true") boolean flash, 
        @RequestParam(name = "flashRate", defaultValue = "5") int flashRate, 
        @RequestParam(name = "targetDelay", defaultValue = "500") int targetDelay, 
        @RequestParam(name = "targetDuration", defaultValue = "3000") int targetDuration,
        @RequestParam(name = "stroopDelay", defaultValue = "100") int stroopDelay, 
        @RequestParam(name = "stroopDuration", defaultValue = "1900") int stroopDuration,
        @RequestParam(name = "colourFilter", defaultValue = "") List<String> colourFilterStr
    ) {
        LOG.info("Resetting targets");
        targetSequenceController.resetTargets();

        if (UNKNOWN_PID.equals(PID)) {
            stroopHandler.sendStroop(new Stroop("Missing PID", StroopColour.RED, 0, 0));
            throw new IllegalStateException("PID has not been generated...");
        }

        LOG.info("Checking map has been updated: %d".formatted(targetConnectionToPhysicalColumnMapping.size()));

        if (targetConnectionToPhysicalColumnMapping.isEmpty()) {
            throw new IllegalStateException("There are no targets presently connected to the system");
        }

        TargetType targetType = TargetType.INDIVIDUAL;
        // TargetType targetType;
        // try {
        //     targetType = TargetType.valueOf(targetTypeStr.toUpperCase());
        // } catch (IllegalArgumentException iaex) {
        //     LOG.error("Unable to parse provided targetType: %s, accepted values are {'CLUSTER', 'INDIVIDUAL'}\nUsing INDIVIDUAL as Target Type.".formatted(targetTypeStr), iaex);
        //     targetType = TargetType.INDIVIDUAL;
        // }
        File file = new File("%s/%s".formatted(rootFilePath, PID));
        file.mkdirs();
        String sessionFileName = "%s/%s/%s_%s_%s_%s".formatted(rootFilePath, PID, pointingBehaviour, targetType.name(), "DISTRACTOR", Helpers.getCurrentDateTimeString());
        targetSequenceController.run(targetType, targetDelay, targetDuration, stroopDelay, stroopDuration, 0, true, flash, flashRate, PID, sessionFileName, targetConnectionToPhysicalColumnMapping, colourFilterStr.stream().map(StroopColour::valueOf).collect(Collectors.toSet()));
    }

    @PostMapping("/resume")
    public void resume(
        @RequestParam(name = "flash", defaultValue = "true") boolean flash, 
        @RequestParam(name = "flashRate", defaultValue = "5") int flashRate
    ) {
        targetSequenceController.resume(flash, flashRate, targetConnectionToPhysicalColumnMapping);
    }

    @RestController
    @RequestMapping(value = "/demo")
    public class DemoRestController {
        @PostMapping("/stroop/colours")
        public void demoStroopColours(@RequestParam(name = "ColourFilter", defaultValue = "") List<String> colourFilterStr) {
            IntStream.range(0, StroopColour.values().length).forEach(i -> {
                targetScheduler.schedule(() -> {
                    StroopColour colour = StroopColour.values()[i];
                    stroopHandler.sendStroop(new Stroop(colour.name(), colour, 0, 0));
                }, (i*500) + (i*3000), TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    stroopHandler.clearStroop();
                }, (i*500) + ((i+1) * 3000), TimeUnit.MILLISECONDS);
            });
        }

        @PostMapping("/stroop/test")
        public void demoStroopTest(@RequestParam(name = "ColourFilter", defaultValue = "") List<String> colourFilterStr) {
            List<Stroop> testStroops = List.of(
                new Stroop(StroopColour.RED.name(), StroopColour.BLUE, 0, 0),
                new Stroop(StroopColour.BROWN.name(), StroopColour.PURPLE, 0, 0),
                new Stroop(StroopColour.GREEN.name(), StroopColour.GREEN, 0, 0),
                new Stroop(StroopColour.PURPLE.name(), StroopColour.RED, 0, 0),
                new Stroop(StroopColour.BLUE.name(), StroopColour.BROWN, 0, 0)
            );
            IntStream.range(0, testStroops.size()).forEach(i -> {
                targetScheduler.schedule(() -> {
                    stroopHandler.sendStroop(testStroops.get(i));
                }, (i*500) + (i*2000), TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    stroopHandler.clearStroop();
                }, (i*500) + ((i+1) * 2000), TimeUnit.MILLISECONDS);
            });
        }

        @PostMapping("/stroop/point")
        public void demoStroopPointingTest(@RequestParam(name = "ColourFilter", defaultValue = "") List<String> colourFilterStr) {
            List<Stroop> testStroops = List.of(
                new Stroop(StroopColour.RED.name(), StroopColour.BLUE, 0, 0),
                new Stroop(StroopColour.BROWN.name(), StroopColour.PURPLE, 0, 0),
                new Stroop(StroopColour.GREEN.name(), StroopColour.GREEN, 0, 0),
                new Stroop(StroopColour.PURPLE.name(), StroopColour.RED, 0, 0),
                new Stroop(StroopColour.BLUE.name(), StroopColour.BROWN, 0, 0),
                new Stroop(StroopColour.GREEN.name(), StroopColour.BROWN, 0, 0),
                new Stroop(StroopColour.BLUE.name(), StroopColour.RED, 0, 0),
                new Stroop(StroopColour.GREEN.name(), StroopColour.PURPLE, 0, 0),
                new Stroop(StroopColour.PURPLE.name(), StroopColour.BLUE, 0, 0),
                new Stroop(StroopColour.RED.name(), StroopColour.BLUE, 0, 0),
                new Stroop(StroopColour.BROWN.name(), StroopColour.BROWN, 0, 0),
                new Stroop(StroopColour.GREEN.name(), StroopColour.GREEN, 0, 0),
                new Stroop(StroopColour.BLUE.name(), StroopColour.PURPLE, 0, 0),
                new Stroop(StroopColour.PURPLE.name(), StroopColour.BROWN, 0, 0),
                new Stroop(StroopColour.BLUE.name(), StroopColour.RED, 0, 0),
                new Stroop(StroopColour.GREEN.name(), StroopColour.BLUE, 0, 0),
                new Stroop(StroopColour.BLUE.name(), StroopColour.GREEN, 0, 0)
            );
            IntStream.range(0, testStroops.size()).forEach(i -> {
                targetScheduler.schedule(() -> {
                    stroopHandler.sendStroop(testStroops.get(i));
                }, (i*500) + (i*2000), TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    stroopHandler.clearStroop();
                }, (i*500) + ((i+1) * 2000), TimeUnit.MILLISECONDS);
            });
            targetScheduler.schedule(this::demoTargetSequence, 5000, TimeUnit.MILLISECONDS);
        }

        @PostMapping("/target/muted")
        public void demoTargetMuted() {
            Target target = new Target(13, 8, 0, 0);
            IntStream.range(0, 5).forEach(i -> {
                targetScheduler.schedule(() -> {
                    targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping, target, false) ;
                }, (i*1200) , TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    targetSequenceController.sendCommand(
                        targetConnectionToPhysicalColumnMapping, 
                        new ResetTarget(target.col), 
                        false,
                        i==4 ? 100 : -1
                    );
                }, (i*600) + ((i+1) * 600), TimeUnit.MILLISECONDS);
            });
        }

        @PostMapping("/target/buzzer")
        public void demoTargetBuzzer() {
            Target target = new Target(13, 8, 0, 0);
            IntStream.range(0, 5).forEach(i -> {
                boolean flash = i==0;
                targetScheduler.schedule(() -> {
                    targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping, target, flash);
                }, (i*1200), TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    targetSequenceController.sendCommand(
                        targetConnectionToPhysicalColumnMapping, 
                        new ResetTarget(target.col), 
                        false,
                        i==4 ? 100 : -1
                    );
                }, (i*600) + ((i+1) * 600), TimeUnit.MILLISECONDS);
            });
        }

        @PostMapping("/target/sequence")
        public void demoTargetSequence() {
            List<Target> testTargets = List.of(
                new Target(3, 0, 0, 0),
                new Target(8, 5, 0, 0),
                new Target(5, 7, 0, 0),
                new Target(10, 2, 0, 0),
                new Target(13, 8, 0, 0)
            );
            IntStream.range(0, testTargets.size()).forEach(j -> {
                Target target = testTargets.get(j);
                IntStream.range(0, 5).forEach( i -> {
                    boolean flash = i==0;
                    targetScheduler.schedule(() -> {
                        targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping, target, flash);
                    }, (j * 4000) + (i*1200), TimeUnit.MILLISECONDS);
                    targetScheduler.schedule(() -> {
                        targetSequenceController.sendCommand(
                            targetConnectionToPhysicalColumnMapping, 
                            new ResetTarget(target.col), 
                            false, 
                            i==4 ? (j+1) / (double)testTargets.size() : -1
                        );
                    }, (j * 4000) + (i*600) + ((i+1) * 600), TimeUnit.MILLISECONDS);
                });
            });
        }
    }

    @RestController
    @RequestMapping(value = "/calibration")
    public class CalibrationRestController {
        // @PostMapping("/colour/{id}")
        // public void colour(@PathVariable(name="id") int colourId) {
        //     LOG.info("Resetting targets");
        //     if (colourId > 3 || colourId < 0) {
        //         throw new IllegalArgumentException("Invalid value given: %d, accepted values {0 (White), 1 (GREEN), 2 (RED), 3 (BLUE)}".formatted(colourId));
        //     }
        //     targetColour.set(colourId);
        // }

        @PostMapping("/reset")
        public void reset() {
            targetSequenceController.resetTargets();
        }

        // @GetMapping("/count")
        // public @ResponseBody int count() {
        //     return targetSockets.size();
        // }

        @PostMapping("/identify/subtargets")
        public void identifySubTargets() {
            targetSequenceController.resetTargets();
            List<Target> targets = IntStream.range(0, targetSockets.size()*3)
                .mapToObj(i -> {
                    return IntStream.range(0, subTargetCount)
                        .mapToObj(j -> new Target(i, j, 250, 1000));
                        // .collect(Collectors.toList());
                }).flatMap(x -> x)
                .collect(Collectors.toList());
            LOG.info("Generated Target Sequence: %d".formatted(targets.size()));
            IntStream.range(0, targets.size()).forEach(i -> {
                LOG.info("Scheduling target %d/%d".formatted(i+1, targets.size()));
                Target target = targets.get(i);
                long delay = ((target.startDelayMilliseconds + target.durationMilliseconds) * i) + target.startDelayMilliseconds;
                targetScheduler.schedule(() -> {
                    try {
                        LOG.info("Identify %d (%s) [(%d,%d),%d] - delay: %d".formatted(target.id, targetConnectionToPhysicalColumnMapping.get(target.col), target.col, target.row, target.subTarget, delay));
                        targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping, target, true);
                    } catch (Exception pkmn) {
                        LOG.error("Failing to send command to identify subtarget", pkmn);
                    }
                }, delay, TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    try {
                        LOG.info("End Identify %d (%s) [(%d,%d),%d] - delay: %d".formatted(target.id, targetConnectionToPhysicalColumnMapping.get(target.col), target.col, target.row, target.subTarget, delay));
                        targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping, new ResetTarget(target.col), false, i+1 / (double) targets.size());
                    } catch (Exception pkmn) {
                        LOG.error("Failing to send command to turnoff subtarget", pkmn);
                    }
                }, delay + target.durationMilliseconds, TimeUnit.MILLISECONDS);
            });
        }

        // @PostMapping("/identify/mapped")
        public void identifyMapping() {
            targetSequenceController.resetTargets();
            for (int i=0; i<targetSockets.size(); i++) {
                targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping, Target.identifyColumn(i), true);
            }
        }
        
        @PostMapping("/identify/socket")
        public void identifySocket() {
            targetSequenceController.resetTargets();
            for (int i=0; i<targetSocketIds.size(); i++) {
                targetSequenceController.sendCommand(targetSockets.get(targetSocketIds.get(i)), Target.identifyColumn(i), true, -1);
            }
        }

        @PostMapping("/bindings")
        public void updateTargetIDMapping(
            @RequestParam(defaultValue = "{\n" + 
                    "  \"0\": 0,\n" + 
                    "  \"1\": 1,\n" + 
                    "  \"2\": 2,\n" + 
                    "  \"3\": 3,\n" + 
                    "  \"4\": 4\n" + 
                    "}") Map<String, String> map
        ) {
            Map<Integer, Integer> processedMap = map.entrySet()
                .stream()
                .collect(Collectors.toMap(x -> Integer.parseInt(x.getKey()), y -> Integer.parseInt(y.getValue())));
            LOG.info("%d - %s".formatted(processedMap.size(), processedMap.entrySet().stream().map((a) -> String.format("(%d,%d)", a.getKey(), a.getValue())).collect(Collectors.joining(", ", "{", "}"))));
            // go through map and change to correct address
            for (Map.Entry<Integer, Integer> entry : processedMap.entrySet()) {
                String socketAddress;
                try {
                    socketAddress = targetSocketIds.get(entry.getValue());
                } catch(IndexOutOfBoundsException ioobex) {
                    LOG.error("Tried assigning a socket that doesn't exist, so skipping", ioobex);
                    socketAddress = "uh oh"; // setting for testing
                }
                targetConnectionToPhysicalColumnMapping.put(entry.getKey(), socketAddress);
            }
            // targetConnectionToPhysicalColumnMapping = processedMap;//.put(newId, oldId);
            LOG.info("Mapping size: %d".formatted(targetConnectionToPhysicalColumnMapping.size()));
            identifyMapping();
        }
    }
}
