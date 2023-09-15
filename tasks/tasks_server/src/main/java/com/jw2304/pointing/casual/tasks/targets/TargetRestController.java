package com.jw2304.pointing.casual.tasks.targets;

import java.io.IOException;
import java.io.OutputStream;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jw2304.pointing.casual.tasks.targets.TargetSequenceController.TargetColour;

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

    HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping;

    AtomicInteger targetColour = new AtomicInteger(2);

    @PostConstruct
    public void initMapping() {
         targetConnectionToPhysicalColumnMapping = new HashMap<>(targetSockets.size());
    }

    @PostMapping("/start")
    public void start(@RequestParam("targetType") String targetType) {
        LOG.info("Resetting targets");
        resetTargets();
        executor.execute(() -> 
            targetSequenceController.run(targetType, targetConnectionToPhysicalColumnMapping, TargetColour.values()[targetColour.get()])
        );
    }

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
        resetTargets();
        targetSequenceController.sendCommand(id, (byte)0b01011010);
    }

    @PostMapping("/map/{old}/{new}")
    public void updateTargetIDMapping(@PathVariable(name="old") int oldId, @PathVariable(name="new") int newId) {
        targetConnectionToPhysicalColumnMapping.put(newId, oldId);
    }



    private void resetTargets() {
        targetSockets.forEach(socket -> {
            try {
                // reset all targets
                socket.getOutputStream().write(new byte[] { 0 });
            } catch (IOException ioex) {
                LOG.error("Unable to send command to socket: %s".formatted(socket.getInetAddress().getHostAddress()), ioex);
            }
        });
    }

}
