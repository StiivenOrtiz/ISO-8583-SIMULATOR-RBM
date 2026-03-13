package com.stiiven0rtiz.iso8583simulatorbackendrbm.controllers;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.NettyServerController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/netty")
public class NettyRestController {

    private final NettyServerController nettyServerController;

    public NettyRestController(NettyServerController nettyServerController) {
        this.nettyServerController = nettyServerController;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startNetty() {
        try {
            nettyServerController.startNetty();
            return ResponseEntity.ok(buildStatus());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopNetty() {
        try {
            nettyServerController.stopNetty();
            return ResponseEntity.ok(buildStatus());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/restart")
    public ResponseEntity<?> restartNetty() {
        try {
            nettyServerController.restartNetty();
            return ResponseEntity.ok(buildStatus());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(buildStatus());
    }

    private Map<String, Object> buildStatus() {
        Map<String, Object> map = new HashMap<>();

        map.put("running", nettyServerController.isRunning());
        map.put("connections", nettyServerController.activeConnections());
        map.put("timestamp", System.currentTimeMillis());

        return map;
    }

}