package com.stiiven0rtiz.iso8583simulatorbackend.iso.config;


// imports
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * IsoJson.java
 *
 * This class represents the ISO8583 fields configuration in JSON format.
 * For loading the configuration, it uses Jackson's ObjectMapper to read
 *
 * @version 1.0
 */
@Getter @Setter
public class IsoJson {
    @JsonProperty("TPDU")
    private FieldDefinition tpdu;
    @JsonProperty("MTI")
    private FieldDefinition mti;
    @JsonProperty("BITMAP")
    private FieldDefinition bitmap;
    @JsonProperty("dataElements")
    private List<FieldDefinition> dataElements;

    /**
     * Loads the ISO8583 field configuration from the JSON file into this instance.
     *
     * @param CONFIG_FILE The path to the configuration file.
     * @param logger The logger instance to use.
     * @return This populated IsoJson instance.
     */
    public IsoJson loadConfiguration(String CONFIG_FILE_PATH, Logger logger) {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = null;

        try {
            logger.info("Trying to read ISO8583 configuration from file system path: {}", CONFIG_FILE_PATH);
            Resource resource = new FileSystemResource(CONFIG_FILE_PATH);

            if (!resource.exists())
                throw new IOException("Configuration file not found at file system path: " + CONFIG_FILE_PATH);
            if (!resource.isReadable())
                throw new IOException("Configuration file is not readable at file system path: " + CONFIG_FILE_PATH + ". Check file permissions.");

            logger.info("Reading ISO8583 configuration from file system path: {}", CONFIG_FILE_PATH);

            inputStream = resource.getInputStream();
            ObjectReader reader = mapper.readerForUpdating(this);
            reader.readValue(inputStream);

            logger.info("IsoJson configuration loaded successfully from file system at path: {}", CONFIG_FILE_PATH);

        } catch (IOException e) {
            logger.error("Failed to load ISO8583 configuration from external file system path: {}. Please ensure the file exists, is correctly formatted, and has read permissions.", CONFIG_FILE_PATH, e);
            throw new RuntimeException("Failed to load ISO8583 configuration from external file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Error closing input stream", e);
                }
            }
        }

        logger.info("Returning IsoJson instance with loaded configuration");
        return this;
    }
}
