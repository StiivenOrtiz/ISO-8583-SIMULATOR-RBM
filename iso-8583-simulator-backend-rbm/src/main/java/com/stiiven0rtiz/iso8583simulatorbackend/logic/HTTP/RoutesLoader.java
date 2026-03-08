package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RoutesLoader {
    @Value("${http-config.PathsFilePath}")
    String httpRoutesFilePath;

    private final String thisId = toString().substring(toString().indexOf("@"));
    private final Logger logger = LoggerFactory.getLogger(RoutesLoader.class);
    private RoutesConfig routesConfig;
    long lastLength = 0;
    long lastModified = 0;

    protected RoutesConfig LoadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(httpRoutesFilePath);

        if (hasConfigChanged(file.lastModified(), file.length()))
            return mapper.readValue(file, RoutesConfig.class);
        else
            return routesConfig;
    }

    private boolean hasConfigChanged(long newLastModified, long newLastLength) {
        if (newLastModified != lastModified || newLastLength != lastLength) {
            logger.info("{} - Detected change in routes configuration file. Last modified: {}, Last length: {}. New last modified: {}, New last length: {}.",
                    thisId, lastModified, lastLength, newLastModified, newLastLength);
            return true;
        }

        return false;
    }


}
