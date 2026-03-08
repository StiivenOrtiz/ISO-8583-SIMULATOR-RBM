package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.HttpProtocolDetector;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.Iso8583ProtocolDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolDetector {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetector.class);

    public static ProtocolType detect(byte[] data) {

        if (HttpProtocolDetector.isHttpProtocol(data)) {
            logger.info("Detecting HTTP protocol");
            return ProtocolType.HTTP;
        }

        if (Iso8583ProtocolDetector.isIso8583(data)) {
            logger.info("Detecting ISO8583 protocol");
            return ProtocolType.ISO8583;
        }

        logger.info("Detecting unknown protocol");
        return ProtocolType.UNKNOWN;
    }

}