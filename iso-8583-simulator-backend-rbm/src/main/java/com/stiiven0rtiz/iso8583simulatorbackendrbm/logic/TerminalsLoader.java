package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.config.txsConfig.TerminalsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TerminalsLoader {
    @Value("${iso-config.posTerminalesFilePath}")
    String posTerminalesFilePath;

    private final Logger logger = LoggerFactory.getLogger(TerminalsLoader.class);

    /**
     * Loads the delay configuration from the specified JSON file.
     *
     * @return TerminalsConfig object containing the loaded configuration.
     * @throws Exception if there is an error reading or parsing the file.
     */
    protected TerminalsConfig loadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(posTerminalesFilePath);
        return cleanConfig(mapper.readValue(file, TerminalsConfig.class));
    }

    protected TerminalsConfig cleanConfig(TerminalsConfig config) throws Exception {
        Map<String, String> posTerminals = config.getPosTerminals();

        Map<String, String> cleaned = new LinkedHashMap<>();
        Set<String> seenKeys = new HashSet<>();
        Set<String> seenValues = new HashSet<>();

        for (Map.Entry<String, String> entry : posTerminals.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (seenKeys.contains(key) || seenValues.contains(value))
                continue;

            seenKeys.add(key);
            seenValues.add(value);
            cleaned.put(key, value);
        }

        posTerminals.clear();
        posTerminals.putAll(cleaned);

        saveConfig(cleaned);

        TerminalsConfig newConfig = new TerminalsConfig();
        newConfig.setPosTerminals(posTerminals);

        return newConfig;
    }

    public int saveTerminal(String terminalName) {
        try {
            TerminalsConfig config = loadConfig();

            if (!config.getPosTerminals().containsValue(terminalName)) {
                logger.debug("Saving new terminal: {}", terminalName);
                addTerminal(config.getPosTerminals(), terminalName);
                saveConfig(config.getPosTerminals());
                return 0;
            } else {
                logger.warn("Terminal {} already exists in the configuration.", terminalName);
                return 1;
            }
        } catch (Exception e) {
            logger.error("Error loading configuration: {}", e.getMessage());
        }

        // Then create a json File with the terminal
        try {
            createConfigFile(terminalName);
            return 0;
        } catch (IOException e) {
            logger.error("Error creating configuration file: {}", e.getMessage());
            return 2;
        }
    }

    public String removeTerminal(String id) {
        try {
            TerminalsConfig config = loadConfig();

            if (config.getPosTerminals().containsKey(id)) {
                logger.debug("Removing terminal with ID: {}", id);
                String terminalName = config.getPosTerminals().get(id);
                config.getPosTerminals().remove(id);
                saveConfig(config.getPosTerminals());
                return terminalName;
            } else {
                logger.warn("Terminal ID {} does not exist in the configuration.", id);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error loading configuration: {}", e.getMessage());
            return null;
        }
    }

    private void createConfigFile(String terminalName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(posTerminalesFilePath);
        TerminalsConfig newConfig = new TerminalsConfig();
        Map<String, String> posTerminals = new LinkedHashMap<>();
        posTerminals.put("1", terminalName);
        newConfig.setPosTerminals(posTerminals);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, newConfig);
    }

    private void addTerminal(Map<String, String> posTerminals, String terminalName) {
        // get max id from keys
        int maxId = posTerminals.keySet().stream()
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;

        posTerminals.put(String.valueOf(maxId), terminalName);
    }

    private void saveConfig(Map<String, String> posTerminals) throws IOException {
        // Save updated config back to file
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(posTerminalesFilePath);
        TerminalsConfig updatedConfig = new TerminalsConfig();

        updatedConfig.setPosTerminals(posTerminals);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, updatedConfig);
    }

    public Map<String, String> getTerminals() throws Exception {
        return loadConfig().getPosTerminals();
    }

    public boolean terminalExists(String terminalName) {
        try {
            TerminalsConfig config = loadConfig();
            return config.getPosTerminals().containsValue(terminalName);
        } catch (Exception e) {
            logger.error("Error loading configuration: {}", e.getMessage());
            return false;
        }
    }
}
