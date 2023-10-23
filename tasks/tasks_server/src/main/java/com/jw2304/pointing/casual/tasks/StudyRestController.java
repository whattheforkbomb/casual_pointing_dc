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

import com.jw2304.pointing.casual.tasks.stroop.StroopController;
import com.jw2304.pointing.casual.tasks.targets.TargetRestController;
import com.jw2304.pointing.casual.tasks.targets.TargetSequenceController;
import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;
import com.jw2304.pointing.casual.tasks.util.Helpers;

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
    StroopController stroophandler;

    // needed? might be fine to just loop over the 5 target columns?
    @Autowired
    ExecutorService executor;

    @PostMapping("/start")
    public void start(@RequestParam("targetType") String targetTypeStr, @RequestParam("distractor") boolean distractor, @RequestParam(name = "targetDelay", defaultValue = "3000") int targetDelay, @RequestParam(name = "targetDuration", defaultValue = "3000") int targetDuration, @RequestParam(name = "stroopDelay", defaultValue = "250") int stroopDelay, @RequestParam(name = "stroopDuration", defaultValue = "3750") int stroopDuration) {
        LOG.info("Resetting targets");
        targetSequenceController.resetTargets();
        TargetType targetType;
        try {
            targetType = TargetType.valueOf(targetTypeStr.toUpperCase());
        } catch (IllegalArgumentException iaex) {
            LOG.error("Unable to parse provided targetType: %s, accepted values are {'CLUSTER', 'INDIVIDUAL'}\nUsing INDIVIDUAL as Target Type.".formatted(targetTypeStr), iaex);
            targetType = TargetType.INDIVIDUAL;
        }
        String sessionFileName = "/home/whiff/data/%s/%s_%s_%s".formatted(PID, targetType.name(), distractor ? "Distracted": "Focussed", Helpers.getCurrentDateTimeString());
        targetSequenceController.run(targetType, targetDelay, targetDuration, stroopDelay, stroopDuration, distractor, PID, sessionFileName);
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
