package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.model.digitalvoucher.ParsedDigitalVoucherField;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.MessageType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util.HexParser.toHexNoSpace;
import static com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.ParserUtils.*;
import static com.stiiven0rtiz.iso8583simulatorbackendrbm.services.TransactionService.generateVDFields;

@Component
@HTTPRequestParserType(HTTPRequestsParsers.DIGITAL_VOUCHER_SIGNATURE)
public final class RequestDVSignature implements HTTPRequestParser {

    @Override
    public Transaction parseHTTPMessage(byte[] rawMessage, DecodedHTTPMetadata metadata) throws Exception {
        int tpduLength = metadata.tpduLength();
        int headerLength = metadata.headerLength();

        String method = getMethod(rawMessage, headerLength, tpduLength);
        String path = getPathWithoutQuery(rawMessage, headerLength, tpduLength);
        Map<String, byte[]> params = extractQueryParams(metadata);

        String terminal = asciiToString(params.get("Terminal"));
        String receipt = asciiToString(params.get("Recibo"));
        String packages = asciiToString(params.get("Paquetes"));
        String package_ = asciiToString(params.get("Paquete"));
        String signature = asciiToString(params.get("Data"));

        Transaction tx = new Transaction();

        tx.setMti(method + " " + path);
        tx.setTerminal(terminal);
        tx.setHexRequest(toHexNoSpace(rawMessage));

        Map<String, ParsedDigitalVoucherField> fields = new HashMap<>();

        ParsedDigitalVoucherField auxField = new ParsedDigitalVoucherField("receipt", receipt.length(), receipt);
        fields.put("receipt", auxField);
        auxField = new ParsedDigitalVoucherField("packages", packages.length(), packages);
        fields.put("packages", auxField);
        auxField = new ParsedDigitalVoucherField("package", package_.length(), package_);
        fields.put("package", auxField);
        auxField = new ParsedDigitalVoucherField("signature", signature.length(), signature);
        fields.put("signature", auxField);

        generateVDFields(tx, fields, MessageType.REQUEST);

        return tx;
    }

}
