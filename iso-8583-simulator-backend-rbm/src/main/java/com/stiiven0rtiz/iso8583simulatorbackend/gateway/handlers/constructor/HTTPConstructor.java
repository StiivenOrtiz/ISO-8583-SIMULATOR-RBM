package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.*;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.HTTPConfig;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.HTTPDefinition;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.BytesParser.bytesToHexNoSpace;

@SupportsProtocol(ProtocolType.HTTP)
public class HTTPConstructor implements ProtocolFrameConstructor {

    private static final Logger logger = LoggerFactory.getLogger(HTTPConstructor.class);
    String thisId = toString().substring(toString().indexOf("@"));

    HTTPConfig httpConfig;

    public HTTPConstructor(HTTPConfig httpConfig) {
        this.httpConfig = httpConfig;
    }

    @Override
    public ProtocolFrame constructMSG(ProtocolFrame input) throws Exception {
        DecodedHTTPMetadata metadata = (DecodedHTTPMetadata) input.metadata();
        byte[] rawMessage = metadata.rawMessage();
        int headerLength = metadata.headerLength();
        int tpduLength = metadata.tpduLength();

        String method = getMethod(rawMessage, headerLength, tpduLength);
        String path = getPathWithoutQuery(rawMessage, headerLength, tpduLength);

        logger.info("{} - Constructing HTTP message with method: {}", thisId, method);
        logger.info("{} - Constructing HTTP message with path: {}", thisId, path);

        HTTPDefinition httpDefinition;

        try {
            httpDefinition = httpConfig.getHTTPDefinition(path, method);
        } catch (Exception e) {
            logger.error("{} - Error constructing HTTP message: {}", thisId, e.getMessage(), e);
            throw new RuntimeException("Error constructing HTTP message: " + e.getMessage(), e);
        }

        Transaction tx = httpDefinition.httpRequestParser().parseHTTPMessage(rawMessage, metadata);

        input.context().setConstructedMessage();

        return new ProtocolFrame(
                ProtocolType.HTTP,
                new ConstructedHTTPMetadata(httpDefinition, tx, getTPDU(rawMessage, tpduLength)),
                input.context()
        );
    }

    private String getMethod(byte[] rawMessage, int headerLength, int tpduLength) {
        String method = new String(rawMessage, tpduLength, headerLength);
        int firstSpace = method.indexOf(' ');
        if (firstSpace == -1) {
            return method; // No space found, return the whole string
        }
        return method.substring(0, firstSpace);
    }

    private String getPathWithoutQuery(byte[] rawMessage, int headerLength, int tpduLength) {
        String method = new String(rawMessage, tpduLength, headerLength);
        int firstSpace = method.indexOf(' ');
        if (firstSpace == -1) {
            return ""; // No space found, return empty path
        }
        String pathWithQuery = method.substring(firstSpace + 1);
        int queryStart = pathWithQuery.indexOf('?');
        if (queryStart == -1) {
            return pathWithQuery; // No query string, return the whole path
        }
        return pathWithQuery.substring(0, queryStart);
    }

    private String getTPDU(byte[] rawMessage, int tpduLength) {

        byte[] tpduBytes = new byte[tpduLength];
        System.arraycopy(rawMessage, 0, tpduBytes, 0, tpduLength);

        return bytesToHexNoSpace(tpduBytes);
    }
}
