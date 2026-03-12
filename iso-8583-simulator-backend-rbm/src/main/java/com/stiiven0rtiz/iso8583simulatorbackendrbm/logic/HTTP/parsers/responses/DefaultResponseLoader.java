package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@HTTPResponseParserType(HTTPResponsesParsers.NOT_MAPPED)
public non-sealed class DefaultResponseLoader implements HTTPResponseParser {
    @Value("${http-config.DefaultResponseFilePath}")
    String httpDefaultResponseFilePath;

    private final String thisId = toString().substring(toString().indexOf("@"));
    private final Logger logger = LoggerFactory.getLogger(DefaultResponseLoader.class);
    private DefaultResponse defaultResponse;
    long lastLength = 0;
    long lastModified = 0;

    private void LoadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(httpDefaultResponseFilePath);

        if (defaultResponse == null || hasConfigChanged(file.lastModified(), file.length())) {
            defaultResponse = mapper.readValue(file, DefaultResponse.class);
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


    @Override
    public Transaction parseHTTPMessage(Transaction transaction) throws Exception {
        LoadConfig();

        String hexS;
        Transaction tx = new Transaction();

        if (defaultResponse.getRawSpacesMode())
            hexS = defaultResponse.getRaw().replace(" ", "");
        else
            hexS = defaultResponse.getRaw();

        tx.setHexResponse(hexS);

        return tx;
    }
}
