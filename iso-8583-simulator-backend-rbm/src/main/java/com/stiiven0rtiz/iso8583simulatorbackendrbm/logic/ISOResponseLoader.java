package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic;


// imports

import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.stiiven0rtiz.iso8583simulatorbackendrbm.utils.IsoUtils.*;
import static com.stiiven0rtiz.iso8583simulatorbackendrbm.utils.UtilsGenerator.generateRandomNumber;

/**
 * ISOResponseLoader.java
 * <p>
 * This class is responsible for loading ISO 8583 response configurations from JSON files
 * and building response messages based on incoming ISO 8583 messages.
 *
 * @version 1.0
 */
@Component
public class ISOResponseLoader {
    @Value("${iso-config.transactions}")
    private String CONFIG_FOLDER;
    private String thisId = toString().substring(toString().indexOf("@"));

    private Logger logger = LoggerFactory.getLogger(ISOResponseLoader.class);

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads the response configuration from a JSON file based on the transaction code.
     *
     * @param txCode The transaction code used to identify the configuration file.
     * @return A map containing the response data elements and their values.
     * @throws Exception If there is an error reading the file or parsing the JSON.
     */
    protected Map<String, String> loadResponse(String txCode) throws Exception {
        File file = new File(CONFIG_FOLDER + txCode + ".json");
        logger.debug("{} - Loading response configuration from file: {}", thisId, file.getAbsolutePath());
        return mapper.readValue(file, Map.class);
    }

    /**
     * Builds an ISO 8583 response message based on the incoming message and the loaded configuration.
     *
     * @param msg The incoming ISO 8583 message.
     * @return The constructed ISO 8583 response message.
     * @throws Exception If there is an error during the response building process.
     */
    public Iso8583Msg buildResponse(Iso8583Msg msg) throws Exception {
        logger.debug("{} - Building response for incoming message.", thisId);

        String txMTI = msg.getMTI().getValue();
        logger.debug("{} - Incoming MTI: {}", thisId, txMTI);
        txMTI = txMTI.replace(" ", "");

        String txCode = (String) msg.getDataElement("P3");
        txCode = txCode.replace(" ", "");
        logger.debug("{} - Transaction Code: {}.", thisId, txCode);

        // the file name is the MTI + the transaction code
        Map<String, String> responseData = loadResponse(txMTI + txCode);
        logger.debug("{} - Response data loaded: {}", thisId, responseData);

        Iso8583Msg response = new Iso8583Msg();

        response.setTPDU(getResponseTPDUString(msg.getTPDU().getValue()));
        logger.debug("{} - Response TPDU set: {}", thisId, response.getTPDU().getValue());

        response.setMTI(responseData.get("MTI"));
        logger.debug("{} - Response MTI set: {}", thisId, response.getMTI().getValue());

        // iterate over the response data and set the data elements
        for (Map.Entry<String, String> entry : responseData.entrySet()) {
            String key = entry.getKey();
            logger.debug("{} - Loaded key: {}", thisId, key);

            // if the key starts with "P" or "S", it is a data element
            if (key.startsWith("P") || key.startsWith("S")) {
                String value = entry.getValue();

                switch (value) {
                    case "SAME" -> {
                        logger.debug("{} - Field {} say SAME, copying from request.", thisId, key);
                        String existingValue = (String) msg.getDataElement(key);
                        if (existingValue != null) {
                            response.setDataElement(key, existingValue);
                            logger.info("{} - Field {} copied SAME value: {}", thisId, key, response.getDataElement(key));
                        } else
                            logger.warn("{} - Field {} not present in request, cannot copy SAME value.", thisId, key);
                    }
                    case "NOW_TIME" -> {
                        logger.debug("{} - Field {} say NOW_TIME, setting current time.", thisId, key);
                        response.setDataElement(key, LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
                        logger.info("{} - Field {} set to NOW_TIME: {}.", thisId, key, response.getDataElement(key));
                    }
                    case "NOW_DATE" -> {
                        logger.debug("{} - Field {} say NOW_DATE, setting current date.", thisId, key);
                        response.setDataElement(key, LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd")));
                        logger.info("{} - Field {} set to NOW_DATE: {}.", thisId, key, response.getDataElement(key));
                    }
                    default -> {
                        response.setDataElement(key, value);
                        logger.info("{} - Field {} set to value: {}", thisId, key, response.getDataElement(key));
                    }
                }

                if (value.startsWith("ALEATORY")) {
                    logger.debug("{} - Field {} say ALEATORY, generating aleatory value.", thisId, key);
                    // Separate by space and get the length
                    String[] parts = value.split(" ");
                    // print parts for debug
                    logger.debug("{} - ALEATORY parts: {}", thisId, (Object) parts);
                    if (parts.length >= 4) {

                        /*
                            types: N        - NOT IMPLEMENTED
                                   B        - NOT IMPLEMENTED
                                   B-BIT    - NOT IMPLEMENTED
                                   Z        - NOT IMPLEMENTED
                                   A        - NOT IMPLEMENTED
                                   AN       - IMPLEMENTED
                                   ANS      - NOT IMPLEMENTED
                         */

                        String type = parts[1];
                        String typeAleatory = parts[2];
                        int length = Integer.parseInt(parts[3]);

                        String randomValue = "";

                        if (type.equals("AN")) {
                            /*
                                typeAleatory: NUMBER        - IMPLEMENTED
                                              WORD          - NOT IMPLEMENTED
                                              ALPHANUMERIC  - NOT IMPLEMENTED
                             */

                            if (typeAleatory.equals("NUMBER"))
                                randomValue = generateRandomNumber(length);
                            else {
                                logger.error("{} - Field {} say ALEATORY, but type aleatory {} is not implemented. Only NUMBER is implemented.", thisId, key, typeAleatory);
                                throw new IllegalArgumentException("ALEATORY field must have type aleatory 'NUMBER'.");
                            }

                            if (parts.length == 6) {
                                // If has word, check if it is right or left
                                String position = parts[4];
                                String word = parts[5];

                                if (position.equals("RIGHT"))
                                    randomValue = word + randomValue;
                                else if (position.equals("LEFT"))
                                    randomValue += word;
                                else {
                                    logger.error("{} - Field {} say ALEATORY, but position {} is not valid. Use 'right' or 'left'.", thisId, key, position);
                                    throw new IllegalArgumentException("ALEATORY field must have position 'right' or 'left'.");
                                }
                            }

                            if (!randomValue.isEmpty()){
                                response.setDataElement(key, randomValue);
                                logger.info("{} - Field {} set to ALEATORY value: {}", thisId, key, response.getDataElement(key));
                            }

                        } else {
                            logger.error("{} - Field {} say ALEATORY, but type {} is not implemented. Only AN is implemented.", thisId, key, type);
                            throw new IllegalArgumentException("ALEATORY field must have type AN.");
                        }
                    } else {
                        logger.error("{} - Field {} say ALEATORY, but not enough parameters. Expected 4: ALEATORY <type> <type aleatory> <length> [right or left if has word] <word>.", thisId, key);
                        throw new IllegalArgumentException("ALEATORY field must have 4 parameters: ALEATORY <type> <type aleatory> <length> [right or left if has word] <word>");
                    }
                }
            }
        }

        // Set the response bitmap
        // Get existing fields from the response data elements without the prefix
        List<String> existingFields = response.getDataElements().keySet().stream()
                .filter(fieldId -> fieldId.startsWith("P") || fieldId.startsWith("S"))
                .map(fieldId -> fieldId.substring(1)) // remove the prefix
                .toList();

        String bitmap = calculatedBitmap(existingFields);
        // separete primary and secondary bitmap (first 8 bytes and the rest)
        String primaryBitmap = bitmap.substring(0, 23); // first 8 bytes (16 bits)
        response.setBitmap(primaryBitmap);
        logger.info("{} - Primary Bitmap set: {}", thisId, response.getBitmap().getValue());

        String secondaryBitmap = bitmap.substring(24); // the rest (if any)
        // remove spaces from secondary bitmap and if it is not empty, set it
        secondaryBitmap = secondaryBitmap.replace(" ", "");
        if (Long.parseLong(secondaryBitmap) != 0){
            response.setDataElement("P1", secondaryBitmap);
            logger.info("{} - Secondary Bitmap set: {}", thisId, response.getDataElement("P1"));
        }

        logger.debug("{} - Reponse has been built.", thisId);
        return response;
    }
}
