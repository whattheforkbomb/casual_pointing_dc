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

import com.jw2304.pointing.casual.tasks.targets.data.TargetColour;
import com.jw2304.pointing.casual.tasks.targets.data.TargetType;

// @RestController
// @RequestMapping("/targets")
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

    public HashMap<Integer, Integer> targetConnectionToPhysicalColumnMapping = new HashMap<Integer, Integer>();

    public AtomicInteger targetColour = new AtomicInteger(2);

    

}
