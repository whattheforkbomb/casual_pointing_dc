package com.jw2304.pointing.casual.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jw2304.pointing.casual.tasks.stroop.StroopController;
import com.jw2304.pointing.casual.tasks.targets.TargetSequenceController;
import com.jw2304.pointing.casual.tasks.targets.data.Target;
import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;
import com.jw2304.pointing.casual.tasks.util.Helpers;

@RestController
@RequestMapping(value = "/study")
public class StudyRestController {

    public static Logger LOG = LoggerFactory.getLogger(StudyRestController.class);

    // Session State
    private String PID = "UNKNOWN";
    public Map<Integer, Integer> targetConnectionToPhysicalColumnMapping = new HashMap<Integer, Integer>();
    // public AtomicInteger targetColour = new AtomicInteger(2);

    @Autowired
    ArrayList<Socket> targetSockets;

    @Value("${data.filepath}")
    String rootFilePath;

    @Value("${targets.expected.subGroup}")
    public int subTargetCount;

    @Autowired
    TargetSequenceController targetSequenceController;

    @Autowired
    StroopController stroophandler;

    // needed? might be fine to just loop over the 5 target columns?
    public final ScheduledExecutorService targetScheduler = Executors.newScheduledThreadPool(2);

    @PostMapping("/start")
    public void start(
        @RequestParam(name = "targetType", defaultValue = "INDIVIDUAL") String targetTypeStr, 
        @RequestParam("distractor") boolean distractor, 
        @RequestParam(name = "targetDelay", defaultValue = "3000") int targetDelay, 
        @RequestParam(name = "targetDuration", defaultValue = "3000") int targetDuration, 
        @RequestParam(name = "stroopDelay", defaultValue = "250") int stroopDelay, 
        @RequestParam(name = "stroopDuration", defaultValue = "3750") int stroopDuration
    ) {
        LOG.info("Resetting targets");
        targetSequenceController.resetTargets();

        if ("UNKNOWN".equals(PID)) {
            generatePID();
            LOG.warn("Generated PID as was missing...: %s".formatted(PID));
        }

        LOG.info("Checking map has been updated: %d".formatted(targetConnectionToPhysicalColumnMapping.size()));

        TargetType targetType;
        try {
            targetType = TargetType.valueOf(targetTypeStr.toUpperCase());
        } catch (IllegalArgumentException iaex) {
            LOG.error("Unable to parse provided targetType: %s, accepted values are {'CLUSTER', 'INDIVIDUAL'}\nUsing INDIVIDUAL as Target Type.".formatted(targetTypeStr), iaex);
            targetType = TargetType.INDIVIDUAL;
        }
        File file = new File("%s/%s".formatted(rootFilePath, PID));
        file.mkdirs();
        String sessionFileName = "%s/%s/%s_%s_%s".formatted(rootFilePath, PID, targetType.name(), distractor ? "Distracted": "Focussed", Helpers.getCurrentDateTimeString());
        targetSequenceController.run(targetType, targetDelay, targetDuration, stroopDelay, stroopDuration, distractor, PID, sessionFileName, targetConnectionToPhysicalColumnMapping);
    }

    @GetMapping("/pid")
    public String getPID() {
        return PID;
    }

    @PostMapping("/pid")
    public String generatePID() {
        PID = UUID.randomUUID().toString();
        return PID;
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

        @GetMapping("/count")
        public @ResponseBody int count() {
            return targetSockets.size();
        }

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
            for (int i = 0; i < targets.size(); i++) {
                LOG.info("Scheduling target %d/%d".formatted(i+1, targets.size()));
                Target target = targets.get(i);
                long delay = ((target.startDelayMilliseconds + target.durationMilliseconds) * i) + target.startDelayMilliseconds;
                targetScheduler.schedule(() -> {
                    LOG.info("Identify %d (%d) [(%d,%d),%d] - delay: %d".formatted(target.id, targetConnectionToPhysicalColumnMapping.get(target.col), target.col, target.row, target.subTarget, delay));
                    targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping.get(target.col), target.getCommandByte(TargetColour.RED, TargetType.INDIVIDUAL));
                }, delay, TimeUnit.MILLISECONDS);
                targetScheduler.schedule(() -> {
                    LOG.info("End Identify %d (%d) [(%d,%d),%d] - delay: %d".formatted(target.id, targetConnectionToPhysicalColumnMapping.get(target.col), target.col, target.row, target.subTarget, delay));
                    targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping.get(target.col), (byte)0);
                }, delay + target.durationMilliseconds, TimeUnit.MILLISECONDS);
            }
        }

        @PostMapping("/identify/mapped")
        public void identifyMapping() {
            targetSequenceController.resetTargets();
            for (int i=0; i<targetSockets.size(); i++) {
                targetSequenceController.sendCommand(targetConnectionToPhysicalColumnMapping.get(i), TargetSequenceController.identifyColumn(i).getCommandByte(TargetColour.RED, TargetType.INDIVIDUAL));
            }
        }
        
        @PostMapping("/identify/socket")
        public void identifySocket() {
            targetSequenceController.resetTargets();
            for (int i=0; i<targetSockets.size(); i++) {
                targetSequenceController.sendCommand(i, TargetSequenceController.identifyColumn(i).getCommandByte(TargetColour.RED, TargetType.INDIVIDUAL));
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
            Map<Integer, Integer> processedMap = map.entrySet().stream()
            .collect(Collectors.toMap(x -> Integer.parseInt(x.getKey()), y -> Integer.parseInt(y.getValue())));
            LOG.info("%d - %s".formatted(processedMap.size(), processedMap.entrySet().stream().map((a) -> String.format("(%d,%d)", a.getKey(), a.getValue())).collect(Collectors.joining(", ", "{", "}"))));
            targetConnectionToPhysicalColumnMapping = processedMap;//.put(newId, oldId);
            LOG.info("Mapping size: %d".formatted(targetConnectionToPhysicalColumnMapping.size()));
        }
    }
}
