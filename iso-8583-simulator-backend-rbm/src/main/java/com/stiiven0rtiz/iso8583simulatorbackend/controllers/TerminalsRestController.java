package com.stiiven0rtiz.iso8583simulatorbackend.controllers;

import com.stiiven0rtiz.iso8583simulatorbackend.dto.AllConfig;
import com.stiiven0rtiz.iso8583simulatorbackend.dto.TerminalConfig;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ConfigurationController;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.TerminalsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terminals")
public class TerminalsRestController {

    Logger logger = LoggerFactory.getLogger(TerminalsRestController.class);

    ConfigurationController configurationController;

    TerminalsRestController(ConfigurationController configurationController) {
        this.configurationController = configurationController;
    }

    @GetMapping
    public ResponseEntity<?> getTerminals() {
        logger.info("Received request to get terminals");
        try {
            return ResponseEntity.ok(configurationController.getAllTerminals());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving terminals");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addTerminal(@RequestParam(required = true) String terminal) {
        logger.info("Received request to add terminal");
        try {
            int result = configurationController.addTerminal(terminal);

            if (result == 0)
                return ResponseEntity.ok("Terminal added successfully");
            else if (result == 1)
                return ResponseEntity.status(409).body("Terminal already exists");
            else
                return ResponseEntity.status(500).body("Error adding terminal");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding terminal");
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeTerminal(@RequestParam(required = true) String id) {
        logger.info("Received request to remove terminal: {}", id);
        try {
            Boolean result = configurationController.deleteTerminal(id);

            if (result)
                return ResponseEntity.ok("Terminal removed successfully");
            else
                return ResponseEntity.status(404).body("Terminal not found");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error removing terminal");
        }
    }

    @PostMapping("/responsecode")
    public ResponseEntity<String> setResponseCode(@RequestParam(required = true) String terminal,
                                                  @RequestParam(required = true) String code) {
        logger.info("Received request to set response code");
        try {
            Boolean result = configurationController.setResponseCode(terminal, code);

            if (result)
                return ResponseEntity.ok("Response code set successfully");
            else
                return ResponseEntity.status(500).body("Error setting response code");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error setting response code");
        }
    }

    @PostMapping("/delay")
    public ResponseEntity<String> setDelay(@RequestParam(required = true) String terminal,
                                           @RequestParam(required = true) int seconds) {
        logger.info("Received request to set delay");
        try {
            Boolean result = configurationController.setDelay(terminal, seconds);

            if (result)
                return ResponseEntity.ok("Delay set successfully");
            else
                return ResponseEntity.status(500).body("Error setting delay");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error setting delay");
        }
    }

    @PostMapping("/globaldelay")
    public ResponseEntity<String> setGlobalDelay(@RequestParam(required = true) int seconds) {
        logger.info("Received request to set global delay");
        try {
            if (configurationController.setGlobalDelay(seconds))
                return ResponseEntity.ok("Global delay set successfully");
            else
                return ResponseEntity.status(500).body("Error setting global delay");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error setting global delay");
        }
    }

    @PostMapping("/globalresponsecode")
    public ResponseEntity<String> setGlobalResponseCode(@RequestParam(required = true) String code) {
        logger.info("Received request to set global response code");
        try {
            if (configurationController.setGlobalResponseCode(code))
                return ResponseEntity.ok("Global response code set successfully");
            else
                return ResponseEntity.status(500).body("Error setting global response code");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error setting global response code");
        }
    }

    @GetMapping("/getallconfig")
    public ResponseEntity<AllConfig> getAllConfig() {
        try{
            AllConfig config = configurationController.getAllConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Failed to get all terminal configurations: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/turnoffglobalresponse")
    public ResponseEntity<String> setOffGlobalResponse(@RequestParam(required = true) String lastCode) {
        logger.info("Received request to turn off global response code");
        try {
            if (configurationController.setOffGlobalResponse(lastCode))
                return ResponseEntity.ok("Global response code turned off successfully");
            else
                return ResponseEntity.status(500).body("Error turning off global response code");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error turning off global response code");
        }
    }

    @PostMapping("/turnoffglobaldelay")
    public ResponseEntity<String> setOffGlobalDelay(@RequestParam(required = true) int lastSeconds) {
        logger.info("Received request to turn off global delay");
        try {
            if (configurationController.setOffGlobalDelay(lastSeconds))
                return ResponseEntity.ok("Global delay turned off successfully");
            else
                return ResponseEntity.status(500).body("Error turning off global delay");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error turning off global delay");
        }
    }
}
