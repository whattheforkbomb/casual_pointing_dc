package com.jw2304.pointing.casual.tasks.targets;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;

@Controller
public class TargetSequenceController {

    @Value("${targets.expected.subGroup}")
    public int targetsPerConnections;

    @Value("${targets.task.count}")
    public int taskCount;

    @Value("${targets.delay.seconds.on}")
    public int targetOnDelay;

    @Value("${targets.delay.seconds.off}")
    public int targetOffDelay;

    @Autowired
    ArrayList<Socket> targetSockets;

    @Autowired
    ExecutorService executor;

    // The command sequence is setup as pairs of bytes, the first being the target, the second being the command
    private byte[] generate(String targetSize) {
        byte[] commandSequence = new byte[taskCount*2];

        // if whole target, we will repeat.
        // if single LED, we will omit the same number of LED from each target.
        
        // Need to uniformly distribute across the columns and rows (e.g. next target should be different column and row; for individual LED need different row column within target, but can reuse target, should not be the same row or column on different target).

        // 

        return commandSequence;
    }

    public void run(String targetSize, HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping) {

        byte[] commandSequence = generate(targetSize);
    }
    
}
