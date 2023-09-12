package com.jw2304.pointing.casual.tasks;

import java.net.ServerSocket;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TargetController {
    
    ServerSocket server = null;

    public TargetController(ServerSocket server) {
        this.server = server;
    }

    @GetMapping("/start")
    HttpStatus start() {


        return HttpStatus.OK;
    }

}
