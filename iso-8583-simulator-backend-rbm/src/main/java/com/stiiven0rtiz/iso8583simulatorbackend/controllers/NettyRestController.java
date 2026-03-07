package com.stiiven0rtiz.iso8583simulatorbackend.controllers;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.NettyServerController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/netty")
public class NettyRestController {

    private final NettyServerController nettyServerController;

    public NettyRestController(NettyServerController nettyServerController) {
        this.nettyServerController = nettyServerController;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startNetty() {
        try {
            nettyServerController.startNetty();
            return ResponseEntity.ok("Netty started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error starting Netty: " + e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopNetty() {
        try {
            nettyServerController.stopNetty();
            return ResponseEntity.ok("Netty stopped successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error stopping Netty: " + e.getMessage());
        }
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartNetty() {
        try {
            nettyServerController.restartNetty();
            return ResponseEntity.ok("Netty restarting...");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error restarting Netty: " + e.getMessage());
        }
    }
}
