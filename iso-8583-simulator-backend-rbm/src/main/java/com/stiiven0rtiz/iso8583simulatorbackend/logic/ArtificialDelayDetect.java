package com.stiiven0rtiz.iso8583simulatorbackend.logic;


// imports

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackend.config.txsConfig.DelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * ArtificialDelayDetect.java
 * <p>
 * This class is responsible for loading artificial delay configurations from a JSON file
 * and determining the appropriate delay for a given terminal ID based on the configuration.
 *
 * @version 1.1
 */
@Component
public class ArtificialDelayDetect {
    @Value("${iso-config.artificialDelayFilePath}")
    String artificialDelayFilePath;

    private final String thisId = toString().substring(toString().indexOf("@"));

    private final Logger logger = LoggerFactory.getLogger(ArtificialDelayDetect.class);

    /**
     * Loads the delay configuration from the specified JSON file.
     *
     * @return DelayConfig object containing the loaded configuration.
     * @throws Exception if there is an error reading or parsing the file.
     */
    protected DelayConfig fastLoadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(artificialDelayFilePath);
        return mapper.readValue(file, DelayConfig.class);
    }

    /**
     * Determines the artificial delay for a given terminal ID based on the loaded configuration.
     *
     * @param terminalId The terminal ID for which to determine the delay.
     * @return The delay in seconds.
     * @throws Exception if there is an error loading the configuration.
     */
    public int getDelay(String terminalId) throws Exception {
        DelayConfig config = fastLoadConfig();
        int delay = 0;

        // Determine delay based on global and per-terminal settings
        if (config.getGlobal().equalsIgnoreCase("on")) {
            delay = config.getGlobalDelay();
            logger.info("{} - Global artificial delay is ON. Delaying for {} seconds", thisId, config.getGlobalDelay());
        } else if (config.getGlobal().equalsIgnoreCase("off")) {
            logger.info("{} - Global artificial delay is OFF.", thisId);
            if (config.getPerTerminal() != null && config.getPerTerminal().containsKey(terminalId)) {
                delay = config.getPerTerminal().get(terminalId);
                logger.info("{} - Artificial delay for terminal {} is set to {} seconds. Delaying...", thisId, terminalId, delay);
            } else
                logger.info("{} - No artificial delay configured for terminal {}.", thisId, terminalId);
        } else
            logger.info("{} - Invalid global delay configuration: {}. No delay applied.", thisId, config.getGlobal());

        return delay;
    }

    /**
     * Retrieves the artificial delay for a given terminal ID without considering global settings.
     *
     * @param terminalId The terminal ID for which to retrieve the delay.
     * @return The delay in seconds.
     * @throws Exception if there is an error loading the configuration.
     */
    public int getDelayForWeb(String terminalId) throws Exception {
        DelayConfig config = fastLoadConfig();
        int delay = 0;

        if (config.getPerTerminal() != null && config.getPerTerminal().containsKey(terminalId))
            delay = config.getPerTerminal().get(terminalId);

        return delay;
    }

    /**
     * Loads and cleans the delay configuration from the specified JSON file.
     * Removes entries with zero delay and duplicates, then saves the cleaned configuration back to the file.
     *
     * @return Cleaned DelayConfig object.
     * @throws Exception if there is an error reading, parsing, or writing the file.
     */
    protected DelayConfig loadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(artificialDelayFilePath);
        return cleanConfig(mapper.readValue(file, DelayConfig.class));
    }

    private DelayConfig cleanConfig(DelayConfig config) throws Exception {

        Map<String, Integer> posTerminals = config.getPerTerminal();

        Map<String, Integer> cleaned = new LinkedHashMap<>();
        Set<String> seenKeys = new HashSet<>();

        for (Map.Entry<String, Integer> entry : posTerminals.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (value != null && value == 0)
                continue;

            if (seenKeys.contains(key))
                continue;

            seenKeys.add(key);
            cleaned.put(key, value);
        }

        posTerminals.clear();
        posTerminals.putAll(cleaned);

        saveConfig(cleaned, config.getGlobal(), config.getGlobalDelay());

        DelayConfig newConfig = new DelayConfig();
        newConfig.setGlobal(config.getGlobal());
        newConfig.setGlobalDelay(config.getGlobalDelay());
        newConfig.setPerTerminal(posTerminals);

        return newConfig;
    }

    private void saveConfig(Map<String, Integer> perTerminal, String global, int globalDelay) throws Exception {
        // Save updated config back to file
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(artificialDelayFilePath);
        DelayConfig updatedConfig = new DelayConfig();

        updatedConfig.setGlobal(global);
        updatedConfig.setGlobalDelay(globalDelay);
        updatedConfig.setPerTerminal(perTerminal);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, updatedConfig);
    }

    public void removeTerminal(String terminal) {
        try {
            DelayConfig delayConfig = loadConfig();
            if (delayConfig.getPerTerminal().containsKey(terminal)) {
                delayConfig.getPerTerminal().remove(terminal);
                saveConfig(delayConfig.getPerTerminal(), delayConfig.getGlobal(), delayConfig.getGlobalDelay());
            }
        } catch (Exception e) {
            logger.error("Failed to remove terminal {} from delay config: {}", terminal, e.getMessage());
        }
    }

    public Boolean setDelay(String terminal, int seconds) {
        try {
            DelayConfig delayConfig = loadConfig();

            if (seconds == 0)
                delayConfig.getPerTerminal().remove(terminal);
            else
                delayConfig.getPerTerminal().put(terminal, seconds);

            saveConfig(delayConfig.getPerTerminal(), delayConfig.getGlobal(), delayConfig.getGlobalDelay());
            return true;
        } catch (Exception e) {
            logger.error("Failed to set delay for terminal {}: {}", terminal, e.getMessage());
            return false;
        }
    }

    public Boolean setGlobalDelay(int seconds) {
        try {
            DelayConfig delayConfig = loadConfig();
            delayConfig.setGlobalDelay(seconds);
            delayConfig.setGlobal("on");
            saveConfig(delayConfig.getPerTerminal(), delayConfig.getGlobal(), seconds);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set global delay: {}", e.getMessage());
            return false;
        }
    }

    public int getGlobalDelay() throws Exception {
        DelayConfig config = fastLoadConfig();
        return config.getGlobalDelay();
    }

    public Boolean isGlobalDelayOn() throws Exception {
        DelayConfig config = fastLoadConfig();
        return config.getGlobal().equalsIgnoreCase("on");
    }

    public Boolean setOffGlobalDelay(int lastSeconds) {
        try {
            DelayConfig delayConfig = loadConfig();
            delayConfig.setGlobalDelay(lastSeconds);
            delayConfig.setGlobal("off");
            saveConfig(delayConfig.getPerTerminal(), "off", lastSeconds);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set off global delay: {}", e.getMessage());
            return false;
        }
    }
}
