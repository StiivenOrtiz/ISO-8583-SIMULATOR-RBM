package com.stiiven0rtiz.iso8583simulatorbackendrbm.config.txsConfig;


// imports

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * ResponseConfig.java
 * <p>
 * This class represents the configuration for ISO 8583 responses,
 * including global settings and per-terminal overrides.
 *
 * @version 1.1
 */
@Getter
@Setter
public class ResponseConfig {
    private String global;
    private String globalResponse;
    private Map<String, String> perTerminal;
}
