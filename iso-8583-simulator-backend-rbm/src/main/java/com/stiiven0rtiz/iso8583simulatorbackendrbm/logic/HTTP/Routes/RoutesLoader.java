package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.Routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RoutesLoader {
    @Value("${http-config.pathsFilePath}")
    private String httpRoutesFilePath;

    private final String thisId = toString().substring(toString().indexOf("@"));
    private final Logger logger = LoggerFactory.getLogger(RoutesLoader.class);
    private PathsConfig routesConfig;
    private long lastLength = 0;
    private long lastModified = 0;

    private void LoadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(httpRoutesFilePath);

        if (hasConfigChanged(file.lastModified(), file.length())) {
            routesConfig = mapper.readValue(file, PathsConfig.class);
            lastModified = file.lastModified();
            lastLength = file.length();
        }
    }

    private boolean hasConfigChanged(long newLastModified, long newLastLength) {
        if (newLastModified != lastModified || newLastLength != lastLength) {
            logger.info("{} - Detected change in routes configuration file. Last modified: {}, Last length: {}. New last modified: {}, New last length: {}.",
                    thisId, lastModified, lastLength, newLastModified, newLastLength);
            return true;
        }

        return false;
    }

    public Path getRoute(String path, String method) throws Exception {
        Path route = null;

        if (routesConfig == null)
            LoadConfig();

        for (Path r : routesConfig.getPaths())
            if (r.getPath().equals(path) && r.getMethod().equals(method)) {
                route = r;
                break;
            }

        return route;
    }
}
