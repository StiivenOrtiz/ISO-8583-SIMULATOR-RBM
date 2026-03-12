package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@HTTPResponseParserType(HTTPResponsesParsers.NOT_MAPPED)
public non-sealed class DefaultResponseLoader implements HTTPResponseParser {
    private final String thisId = toString().substring(toString().indexOf("@"));
    private final Logger logger = LoggerFactory.getLogger(DefaultResponseLoader.class);

    private final String filePath;
    private DefaultResponse defaultResponse;

    public DefaultResponseLoader(String filePath) { this.filePath = filePath; }

    private void LoadConfig() throws Exception {
        logger.debug("{} - Loading default response from file: {}", thisId, filePath);
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);

        defaultResponse = mapper.readValue(file, DefaultResponse.class);
    }

    @Override
    public Transaction parseHTTPMessage(Transaction transaction) throws Exception {
        LoadConfig();

        logger.debug("{} - Loaded default response, starting to parse transaction.", thisId);

        String hexS;
        Transaction tx = new Transaction();

        tx.setTerminal(transaction.getTerminal());

        if (defaultResponse.getRawSpacesMode())
            hexS = defaultResponse.getRaw().replace(" ", "");
        else
            hexS = defaultResponse.getRaw();

        tx.setHexResponse(hexS);

        return tx;
    }
}
