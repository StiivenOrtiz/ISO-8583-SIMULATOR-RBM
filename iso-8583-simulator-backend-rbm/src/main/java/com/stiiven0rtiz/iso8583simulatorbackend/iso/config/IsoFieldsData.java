package com.stiiven0rtiz.iso8583simulatorbackend.iso.config;


// imports
import jakarta.annotation.PostConstruct;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IsoFields.java
 *
 * This class represents the ISO8583 fields configuration.
 *
 * @version 1.0
 */
@Component
public class IsoFieldsData {
    /**
     * The Logger instance for logging.
     */
    private static final Logger logger = LoggerFactory.getLogger(IsoFieldsData.class);
    /**
     * The Configuration file name and location for ISO8583 fields.
     */
    @Value("${iso-config.file:settings/iso8583-config.json}")
    private String CONFIG_FILE;

    /**
     * The TPDU field definition.
     */
    @Getter
    private FieldDefinition TPDU;
    /**
     * The MTI field definition.
     */
    @Getter
    private FieldDefinition MTI;
    /**
     * The Bitmap field definition.
     */
    @Getter
    private FieldDefinition BITMAP;
    /**
     * The list of data elements.
     */
    @Getter
    private Map<String, FieldDefinition> dataElementsMap;

    /**
     * Loads the ISO8583 field configuration from the IsoJson class after reading the configuration file.
     */
    @PostConstruct
    private void loadConfiguration() {
        logger.info("IsoFieldsData is instantiated. Starting to load configuration...");

        IsoJson loadedData = new IsoJson();
        loadedData = loadedData.loadConfiguration(CONFIG_FILE, logger);

        logger.info("Setting TPDU, MTI, BITMAP and dataElements from IsoJson to IsoFieldsData.");

        this.TPDU = loadedData.getTpdu();
        this.MTI = loadedData.getMti();
        this.BITMAP = loadedData.getBitmap();
        this.setDataElements(loadedData.getDataElements());

        logger.info("ISO8583 fields configuration loaded on IsoFieldsData successfully from IsoJson, starting verification...");

        verifyConfiguration();
    }

    /**
     * Sets the data elements map from the provided list of FieldDefinition objects.
     *
     * @param dataElements The list of FieldDefinition objects to set.
     */
    private void setDataElements(List<FieldDefinition> dataElements) {
        if (dataElements != null) {
            this.dataElementsMap = new ConcurrentHashMap<>();
            for (FieldDefinition element : dataElements) {
                this.dataElementsMap.put(element.id(), element);
            }
        }
    }

    /**
     * Returns the data elements map.
     *
     * @return The map of data elements.
     */
    public FieldDefinition getDataElementById(String id) {
        return dataElementsMap != null ? dataElementsMap.get(id) : null;
    }

    /**
     * Verifies that all required fields are populated and logs the configuration status.
     */
    private void verifyConfiguration() {
        if (this.TPDU != null && this.MTI != null && this.BITMAP != null && this.dataElementsMap != null && !this.dataElementsMap.isEmpty()) {
            logger.info("Configuration verification. All fields are populated.");
            logger.info("TPDU: {} with {} for length.", TPDU.name(), TPDU.length());
            logger.info("MTI: {} with {} for length.", MTI.name(), MTI.length());
            logger.info("BITMAP: {} with {} for length.", BITMAP.name(), BITMAP.length());
            logger.info("Total DataElements loaded: {}", dataElementsMap.size());
        } else {
            logger.error("Configuration verification failed. Some fields are null or dataElementsMap is empty.");
        }
    }
}