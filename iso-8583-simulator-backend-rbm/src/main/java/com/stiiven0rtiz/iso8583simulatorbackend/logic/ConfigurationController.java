package com.stiiven0rtiz.iso8583simulatorbackend.logic;

import com.stiiven0rtiz.iso8583simulatorbackend.dto.AllConfig;
import com.stiiven0rtiz.iso8583simulatorbackend.dto.TerminalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfigurationController {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    TerminalsLoader terminalsLoader;
    ArtificialDelayDetect artificialDelayDetect;
    ResponseCodeLoader responseCodeLoader;


    public ConfigurationController(TerminalsLoader terminalsLoader,
                                   ArtificialDelayDetect artificialDelayDetect,
                                   ResponseCodeLoader responseCodeLoader) {
        this.terminalsLoader = terminalsLoader;
        this.artificialDelayDetect = artificialDelayDetect;
        this.responseCodeLoader = responseCodeLoader;
    }

    public Map<String, String> getAllTerminals() throws Exception {
        return terminalsLoader.getTerminals();
    }

    public int addTerminal(String terminal) {
        return terminalsLoader.saveTerminal(terminal);
    }

    public Boolean setResponseCode(String terminal, String code) {
        if (terminalsLoader.terminalExists(terminal)) {
            return responseCodeLoader.setResponseCode(terminal, code);
        } else {
            logger.error("Terminal {} does not exist. Cannot set response code.", terminal);
            return false;
        }
    }

    public Boolean setDelay(String terminal, int seconds) {
        if (terminalsLoader.terminalExists(terminal)) {
            return artificialDelayDetect.setDelay(terminal, seconds);
        } else {
            logger.error("Terminal {} does not exist. Cannot set delay.", terminal);
            return false;
        }
    }

    public Boolean deleteTerminal(String terminal) {
        try {
            String result = terminalsLoader.removeTerminal(terminal);
            if (result != null) {
                artificialDelayDetect.removeTerminal(terminal);
                responseCodeLoader.removeTerminal(terminal);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to delete terminal {}: {}", terminal, e.getMessage());
            return false;
        }
        return false;
    }

    public Boolean setGlobalDelay(int seconds) {
        try {
            return artificialDelayDetect.setGlobalDelay(seconds);
        } catch (Exception e) {
            logger.error("Failed to set global delay: {}", e.getMessage());
            return false;
        }
    }

    public Boolean setGlobalResponseCode(String code) {
        try {
            return responseCodeLoader.setGlobalResponseCode(code);
        } catch (Exception e) {
            logger.error("Failed to set global response code: {}", e.getMessage());
            return false;
        }
    }

    public Boolean setOffGlobalResponse(String lastCode) {
        try {
            return responseCodeLoader.setOffGlobalResponse(lastCode);
        } catch (Exception e) {
            logger.error("Failed to set off global response code: {}", e.getMessage());
            return false;
        }
    }

    public Boolean setOffGlobalDelay(int lastSeconds) {
        try {
            return artificialDelayDetect.setOffGlobalDelay(lastSeconds);
        } catch (Exception e) {
            logger.error("Failed to set off global delay: {}", e.getMessage());
            return false;
        }
    }

    public AllConfig getAllConfig() {
        try {

            Map<String, String> terminals = getAllTerminals();

            return new AllConfig(
                    artificialDelayDetect.isGlobalDelayOn(),
                    artificialDelayDetect.getGlobalDelay(),
                    responseCodeLoader.isGlobalResponseOn(),
                    responseCodeLoader.getGlobalResponseCode(),
                    terminals.entrySet().stream().map(entry -> {
                        String terminalID = entry.getValue();
                        String idDB = entry.getKey();
                        int delay = 0;

                        try {
                            delay = artificialDelayDetect.getDelayForWeb(terminalID);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        String responseCode = responseCodeLoader.getResponseCode(terminalID);

                        if (responseCode == null || responseCode.isEmpty())
                            responseCode = "";

                        return new TerminalConfig(idDB, terminalID, delay, responseCode);
                    }).toList()
            );


        } catch (Exception e) {
            logger.error("Failed to get all terminal configurations: {}", e.getMessage());
            return null;
        }
    }

}
