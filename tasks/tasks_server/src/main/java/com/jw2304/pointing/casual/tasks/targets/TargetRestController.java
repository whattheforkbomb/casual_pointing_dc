package com.jw2304.pointing.casual.tasks.targets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

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

    @PostConstruct
    public void initMapping() {
         targetConnectionToPhysicalColumnMapping = new HashMap<>(targetSockets.size());
    }

    @PostMapping("/start")
    public void start(@RequestParam("targetSize") String targetSize) {
        LOG.info("Resetting targets");
        resetTargets();
        executor.execute(() -> 
            targetSequenceController.run(targetSize, targetConnectionToPhysicalColumnMapping)
        );
    }

    @GetMapping("/count")
    public @ResponseBody int count() {
        return targetSockets.size();
    }

    @PostMapping("/identify/{id}")
    public void identify(@PathVariable(value="id") int id) {
        resetTargets();

    }

    @PostMapping("/map/{old}/{new}")
    public void updateTargetIDMapping(@PathVariable(value="old") int oldId, @PathVariable(value="new") int newId) {
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
