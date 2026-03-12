package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic;


// imports

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.config.txsConfig.ResponseConfig;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;
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
 * ResponseCodeLoader.java
 * <p>
 * This class is responsible for loading ISO 8583 response code configurations from a JSON file
 * and validating/modifying the response codes of incoming ISO 8583 messages based on the configuration.
 *
 * @version 1.1
 */
@Component
public class ResponseCodeLoader {
    @Value("${iso-config.responseCodesFilePath}")
    String responseCodesFilePath;

    private final String thisId = toString().substring(toString().indexOf("@"));
    private final Logger logger = LoggerFactory.getLogger(ResponseCodeLoader.class);

    /**
     * Loads the response code configuration from a JSON file.
     *
     * @return ResponseConfig object containing the configuration.
     * @throws Exception if there is an error reading or parsing the file.
     */
    protected ResponseConfig fastLoadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(responseCodesFilePath);
        return mapper.readValue(file, ResponseConfig.class);
    }

    /**
     * Validates and modifies the response code of the given ISO 8583 message based on the loaded configuration.
     *
     * @param msg The ISO 8583 message to be validated/modified.
     * @throws Exception if there is an error loading the configuration.
     */
    public void validateResponseCode(Iso8583Msg msg) throws Exception {
        ResponseConfig config = fastLoadConfig();
        String terminalId = (String) msg.getDataElement("P41");
        int delay = 0;

        // first, it checks if there's a configured delay for all terminals
        if (config.getGlobal().equalsIgnoreCase("on")) {
            String newCode = config.getGlobalResponse();
            logger.info("{} - Global response code is ON. Changing response code to {}", thisId, newCode);
            msg.setDataElement("P39", newCode);
        } else if (config.getGlobal().equalsIgnoreCase("off")) { // if global is off, it checks for per-terminal configuration
            logger.info("{} - Global response code is OFF.", thisId);
            if (config.getPerTerminal() != null && config.getPerTerminal().containsKey(terminalId)) {
                String newCode = config.getPerTerminal().get(terminalId);
                logger.info("{} - Response code for terminal {} is set to {}. Changing response code...", thisId, terminalId, newCode);
                msg.setDataElement("P39", newCode);
            } else
                logger.info("{} - No response code configured for terminal {}.", thisId, terminalId);
        } else
            logger.info("{} - Invalid global response code configuration: {}. No changes applied.", thisId, config.getGlobal());
    }

    protected ResponseConfig loadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(responseCodesFilePath);
        return cleanConfig(mapper.readValue(file, ResponseConfig.class));
    }

    private ResponseConfig cleanConfig(ResponseConfig config) throws Exception {

        Map<String, String> posTerminals = config.getPerTerminal();

        Map<String, String> cleaned = new LinkedHashMap<>();
        Set<String> seenKeys = new HashSet<>();

        for (Map.Entry<String, String> entry : posTerminals.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value != null && value.equals("00"))
                continue;

            if (seenKeys.contains(key))
                continue;

            seenKeys.add(key);
            cleaned.put(key, value);
        }

        posTerminals.clear();
        posTerminals.putAll(cleaned);

        saveConfig(cleaned, config.getGlobal(), config.getGlobalResponse());

        ResponseConfig newConfig = new ResponseConfig();
        newConfig.setGlobal(config.getGlobal());
        newConfig.setGlobalResponse(config.getGlobalResponse());
        newConfig.setPerTerminal(posTerminals);

        return newConfig;
    }

    private void saveConfig(Map<String, String> perTerminal, String global, String globalResponse) throws Exception {
        // Save updated config back to file
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(responseCodesFilePath);
        ResponseConfig updatedConfig = new ResponseConfig();

        updatedConfig.setGlobal(global);
        updatedConfig.setGlobalResponse(globalResponse);
        updatedConfig.setPerTerminal(perTerminal);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, updatedConfig);
    }

    public void removeTerminal(String terminal) {
        try {
            ResponseConfig responseConfig = loadConfig();
            if (responseConfig.getPerTerminal().containsKey(terminal)) {
                responseConfig.getPerTerminal().remove(terminal);
                saveConfig(responseConfig.getPerTerminal(), responseConfig.getGlobal(), responseConfig.getGlobalResponse());
            }
        } catch (Exception e) {
            logger.error("Failed to remove terminal {} from response code config: {}", terminal, e.getMessage());
        }
    }

    public Boolean setResponseCode(String terminal, String code) {
        try {
            ResponseConfig responseConfig = loadConfig();

            if (code.equals("00"))
                responseConfig.getPerTerminal().remove(terminal);
            else
                responseConfig.getPerTerminal().put(terminal, code);

            saveConfig(responseConfig.getPerTerminal(), responseConfig.getGlobal(), responseConfig.getGlobalResponse());
            return true;
        } catch (Exception e) {
            logger.error("Failed to set response code for terminal {}: {}", terminal, e.getMessage());
            return false;
        }
    }

    public String getResponseCode(String terminal) {
        try {
            ResponseConfig responseConfig = loadConfig();
            return responseConfig.getPerTerminal().get(terminal);
        } catch (Exception e) {
            logger.error("Failed to get response code for terminal {}: {}", terminal, e.getMessage());
            return null;
        }
    }

    public String getGlobalResponseCode() {
        try {
            ResponseConfig responseConfig = loadConfig();
            return responseConfig.getGlobalResponse();
        } catch (Exception e) {
            logger.error("Failed to get global response code: {}", e.getMessage());
            return null;
        }
    }

    public Boolean isGlobalResponseOn() {
        try {
            ResponseConfig responseConfig = loadConfig();
            return responseConfig.getGlobal().equalsIgnoreCase("on");
        } catch (Exception e) {
            logger.error("Failed to check if global response is on: {}", e.getMessage());
            return false;
        }
    }

    public Boolean setGlobalResponseCode(String code) {
        try {
            ResponseConfig responseConfig = loadConfig();
            responseConfig.setGlobalResponse(code);
            responseConfig.setGlobal("on");
            saveConfig(responseConfig.getPerTerminal(), responseConfig.getGlobal(), code);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set global response code: {}", e.getMessage());
            return false;
        }
    }

    public Boolean setOffGlobalResponse(String lastCode) {
        try {
            ResponseConfig responseConfig = loadConfig();

            responseConfig.setGlobalResponse(lastCode);
            responseConfig.setGlobal("off");

            saveConfig(responseConfig.getPerTerminal(), "off", lastCode);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set global response code to off: {}", e.getMessage());
            return false;
        }
    }
}
