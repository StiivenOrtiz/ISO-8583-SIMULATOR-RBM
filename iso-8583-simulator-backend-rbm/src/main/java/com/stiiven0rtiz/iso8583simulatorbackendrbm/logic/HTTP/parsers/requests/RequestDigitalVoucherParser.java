package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.model.digitalvoucher.ParsedDigitalVoucherField;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.model.digitalvoucher.ParsedDigitalVoucherMessage;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.ParserUtils;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests.schema.digitalvoucher.DigitalVoucherSchema;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests.schema.digitalvoucher.SchemaVDVariableDataFields;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.MessageType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util.BytesParser.parseHexWithoutSpacesString;
import static com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util.HexParser.toHexNoSpace;
import static com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.ParserUtils.*;
import static com.stiiven0rtiz.iso8583simulatorbackendrbm.services.TransactionService.generateVDFields;

@Component
@HTTPRequestParserType(HTTPRequestsParsers.DIGITAL_VOUCHER)
public final class RequestDigitalVoucherParser implements HTTPRequestParser {

    private final String thisId = toString().substring(toString().indexOf("@"));
    private final Logger logger = LoggerFactory.getLogger(RequestDigitalVoucherParser.class);

    @Value("${http-config.parsers.requests.digitalVoucherFilePath}")
    private String digitalVoucherFilePath;
    private long lastLength = 0;
    private long lastModified = 0;
    private DigitalVoucherSchema digitalVoucherSchema;

    private void LoadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(digitalVoucherFilePath);

        if (digitalVoucherSchema == null || hasConfigChanged(file.lastModified(), file.length())) {
            digitalVoucherSchema = mapper.readValue(file, DigitalVoucherSchema.class);
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

    private ParsedDigitalVoucherMessage parse(byte[] message) {
        ParsedDigitalVoucherMessage result = new ParsedDigitalVoucherMessage();
        int offset = 0;

        // STX
        result.setSTX(ParserUtils.bytesToHex(slice(message, offset, digitalVoucherSchema.getSTX().getBytes())));
        offset += digitalVoucherSchema.getSTX().getBytes();

        // Length (BCD)
        byte[] lengthBytes = slice(message, offset, digitalVoucherSchema.getLength().getBytes());
        result.setLength(ParserUtils.bcdToInt(lengthBytes));
        offset += digitalVoucherSchema.getLength().getBytes();

        // typeTransaction
        result.setTypeTransaction(ParserUtils.bytesToHex(slice(message, offset, digitalVoucherSchema.getTypeTransaction().getBytes())));
        offset += digitalVoucherSchema.getTypeTransaction().getBytes();

        // startSeparator
        result.setStartSeparator(ParserUtils.bytesToHex(slice(message, offset, digitalVoucherSchema.getStartSeparator().getBytes())));
        offset += digitalVoucherSchema.getStartSeparator().getBytes();

        // variableDataFields
        parseVariableFields(result, slice(message, offset, message.length - offset - 2), digitalVoucherSchema.getVariableDataFields());
        // ETX
        offset = message.length - 2; // assuming ETX + LRC at the end
        result.setETX(ParserUtils.bytesToHex(slice(message, offset, 1)));

        // LRC
        result.setLRC(ParserUtils.bytesToHex(slice(message, offset + 1, 1)));

        return result;
    }

    private void parseVariableFields(ParsedDigitalVoucherMessage result, byte[] data, SchemaVDVariableDataFields variableData) {
        int offset = 0;
        byte separator = (byte) Integer.parseInt(variableData.separatorFields, 16);

        while (offset < data.length) {

            if (data[offset] == separator || isETX(data[offset])) {
                offset++;
                continue;
            }
            if (offset + 2 > data.length) break;

            byte[] fieldBytes = Arrays.copyOfRange(data, offset, offset + 2);
            String fieldName = asciiToString(fieldBytes);
            offset += 2;

            if (offset + 2 > data.length) break;

            byte[] lengthBytes = Arrays.copyOfRange(data, offset, offset + 2);
            int lengthOfData = ((lengthBytes[0] & 0xFF) << 8) | (lengthBytes[1] & 0xFF);
            offset += 2;

            if (offset + lengthOfData > data.length) break;

            byte[] valueBytes = Arrays.copyOfRange(data, offset, offset + lengthOfData);
            String value = asciiToString(valueBytes);
            offset += lengthOfData;

            if (offset < data.length && data[offset] == separator) {
                offset++;
            }

            result.getVariableFields().add(new ParsedDigitalVoucherField(fieldName, lengthOfData, value));
        }
    }

    private boolean isETX(byte b) {
        return b == 0x03;
    }

    private byte[] slice(byte[] array, int start, int length) {
        byte[] slice = new byte[length];
        System.arraycopy(array, start, slice, 0, length);
        return slice;
    }

    @Override
    public Transaction parseHTTPMessage(byte[] rawMessagem, DecodedHTTPMetadata decodedHTTPMetadata) throws Exception {
        LoadConfig();

        if (digitalVoucherSchema == null) {
            logger.error("{} - Digital Voucher schema is not loaded. Cannot parse message.", thisId);
            throw new RuntimeException("Digital Voucher schema is not loaded. Cannot parse message.");
        }

        Map<String, byte[]> params = ParserUtils.extractQueryParams(decodedHTTPMetadata);

        String terminal = asciiToString(params.get("Terminal"));
        String data = asciiToString(params.get("Data"));
        String method = getMethod(rawMessagem, decodedHTTPMetadata.headerLength(), decodedHTTPMetadata.tpduLength());
        String path = getPathWithoutQuery(rawMessagem, decodedHTTPMetadata.headerLength(), decodedHTTPMetadata.tpduLength());

        logger.debug("{} - param terminal {} param data {}", thisId, terminal, data);

        ParsedDigitalVoucherMessage parsedDigitalVoucherMessage = parse(parseHexWithoutSpacesString(data));

        Map<String, ParsedDigitalVoucherField> fields = parsedDigitalVoucherMessage.getVariableFields()
                .stream()
                .collect(Collectors.toMap(
                        ParsedDigitalVoucherField::getName,
                        Function.identity()));

        Transaction tx = new Transaction();

        tx.setTerminal(terminal);
        tx.setMti(method + " " + path);

        ParsedDigitalVoucherField auxField;

        auxField = fields.get("00");
        tx.setResponseCode(auxField == null ? null : auxField.getValue());
        auxField = fields.get("01");
        tx.setAuthCode(auxField == null ? null : auxField.getValue());
        auxField = fields.get("35");
        tx.setFranchise(auxField == null ? null : auxField.getValue());
        auxField = fields.get("40");
        tx.setAmount(auxField == null ? null : new BigDecimal(auxField.getValue()).movePointLeft(0));
        auxField = fields.get("77");
        tx.setRrn(auxField == null ? null : auxField.getValue());

        tx.setHexRequest(toHexNoSpace(rawMessagem));
        generateVDFields(tx, fields, MessageType.REQUEST);

        return tx;
    }
}
