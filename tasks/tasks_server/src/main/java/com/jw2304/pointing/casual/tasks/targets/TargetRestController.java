package com.jw2304.pointing.casual.tasks.targets;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/targets")
public class TargetRestController {
    
    public static Logger LOG = LoggerFactory.getLogger(TargetRestController.class);

    ServerSocket server = null;

    @Autowired
    ArrayList<Socket> targetSockets;

    // needed? might be fine to just loop over the 5 target columns?
    @Autowired
    ExecutorService executor;

    @Autowired
    TargetSequenceController targetSequenceController;

    public HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping;

    public AtomicInteger targetColour = new AtomicInteger(2);

    @PostConstruct
    public void initMapping() {
         targetConnectionToPhysicalColumnMapping = new HashMap<>(targetSockets.size());
    }

    // @PostMapping("/start")
    // public void start(@RequestParam("targetType") String targetType, @RequestParam("participantId") String participantId) {
    //     // LOG.info("Resetting targets");
    //     // targetSequenceController.resetTargets();
    //     // executor.execute(() -> 
    //     //     targetSequenceController.run(targetType, targetConnectionToPhysicalColumnMapping, TargetColour.values()[targetColour.get()],participantId)
    //     // );
    // }

    @PostMapping("/colour/{id}")
    public void colour(@PathVariable(name="id") int colourId) {
        LOG.info("Resetting targets");
        if (colourId > 3 || colourId < 0) {
            throw new IllegalArgumentException("Invalid value given: %d, accepted values {0 (White), 1 (GREEN), 2 (RED), 3 (BLUE)}".formatted(colourId));
        }
        targetColour.set(colourId);
    }

    @GetMapping("/count")
    public @ResponseBody int count() {
        return targetSockets.size();
    }

    @PostMapping("/identify/{id}")
    public void identify(@PathVariable(name="id") int id) {
        targetSequenceController.resetTargets();
        targetSequenceController.sendCommand(id, TargetSequenceController.IDENTIFY);
    }

    @PostMapping("/map/{old}/{new}")
    public void updateTargetIDMapping(@PathVariable(name="old") int oldId, @PathVariable(name="new") int newId) {
        targetConnectionToPhysicalColumnMapping.put(newId, oldId);
    }

}
