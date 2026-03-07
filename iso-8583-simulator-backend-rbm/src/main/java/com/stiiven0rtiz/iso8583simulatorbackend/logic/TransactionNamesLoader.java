package com.stiiven0rtiz.iso8583simulatorbackend.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackend.config.txsConfig.TransactionNamesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class TransactionNamesLoader {
    @Value("${iso-config.transactionNamesFilePath}")
    String transactionNamesFilePath;

    private final Logger logger = LoggerFactory.getLogger(TransactionNamesLoader.class);

    protected TransactionNamesConfig fastLoadConfig() throws Exception {
        logger.info("Loading transaction names from: " + transactionNamesFilePath);
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(transactionNamesFilePath);
        return mapper.readValue(file, TransactionNamesConfig.class);
    }

    public TransactionNamesConfig getTransactionNames() {
        try{
            return fastLoadConfig();
        } catch (Exception e) {
            logger.error("Error loading transaction names from: {}", transactionNamesFilePath, e);
            return null;
        }
    }
}
