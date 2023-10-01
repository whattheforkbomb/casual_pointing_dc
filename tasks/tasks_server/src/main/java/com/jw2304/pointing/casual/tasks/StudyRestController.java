package com.jw2304.pointing.casual.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jw2304.pointing.casual.tasks.stroop.StroopDistractorController;
import com.jw2304.pointing.casual.tasks.stroop.StroopWebSocketHandler;
import com.jw2304.pointing.casual.tasks.targets.TargetRestController;
import com.jw2304.pointing.casual.tasks.targets.TargetSequenceController;
import com.jw2304.pointing.casual.tasks.targets.TargetSequenceController.TargetColour;

@RestController
@RequestMapping(value = "/study")
public class StudyRestController {

    public static Logger LOG = LoggerFactory.getLogger(StudyRestController.class);

    private String PID = "UNKOWN";

    @Autowired
    TargetSequenceController targetSequenceController;

    @Autowired
    TargetRestController targetRestController;

    @Autowired
    StroopWebSocketHandler stroophandler;

    // needed? might be fine to just loop over the 5 target columns?
    @Autowired
    ExecutorService executor;

    @PostMapping("/start")
    public void start(@RequestParam("targetType") String targetType, @RequestParam("distractor") boolean distractor) {
        LOG.info("Resetting targets");
        targetSequenceController.resetTargets();
        // executor.execute(() -> 
        //     targetSequenceController.run(targetType, targetRestController.targetConnectionToPhysicalColumnMapping, TargetColour.values()[targetRestController.targetColour.get()],participantId)
        // );
        //  executor.execute(() -> 
        //     targetSequenceController.run(targetType, targetRestController.targetConnectionToPhysicalColumnMapping, TargetColour.values()[targetRestController.targetColour.get()],participantId)
        // );
        if (distractor) {
            executor.execute(() -> {
                stroophandler.start();
            });
        }
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
}
