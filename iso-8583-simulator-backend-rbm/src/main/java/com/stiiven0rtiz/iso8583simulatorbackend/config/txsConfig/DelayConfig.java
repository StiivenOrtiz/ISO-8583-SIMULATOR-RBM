package com.stiiven0rtiz.iso8583simulatorbackend.config.txsConfig;


// imports
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * DelayConfig.java
 * This class represents the configuration for global and per-terminal delays.
 * It includes fields for global delay settings and a map for terminal-specific delays.
 *
 * @version 1.1
 */
@Setter
@Getter
public class DelayConfig {
    private String global;
    private int globalDelay;
    private Map<String, Integer> perTerminal;
}