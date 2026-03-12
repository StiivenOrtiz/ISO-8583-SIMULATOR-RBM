package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.response;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.*;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SupportsProtocol(ProtocolType.HTTP)
public class HTTPResponse implements ProtocolResponse {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectorResponse.class);
    String thisId = toString().substring(toString().indexOf("@"));

    @Override
    public ProtocolFrame response(ProtocolFrame input) throws Exception {

        ConstructedHTTPMetadata metadata = (ConstructedHTTPMetadata) input.metadata();

        ResponseMSGHTTP responseMSGHTTP = new ResponseMSGHTTP(metadata.httpDefinition()
                .httpResponseParser().parseHTTPMessage(metadata.drawTransaction()));

        String reorderedTPDU = getReorderedTPDU(metadata);

        // add TPDU to the beginning of the response
        responseMSGHTTP.transaction().setHexResponse(reorderedTPDU + responseMSGHTTP.transaction().getHexResponse());

        ProtocolMetadata responseMetadata = new ResponseMetadata(
                responseMSGHTTP,
                responseMSGHTTP.transaction().getHexResponse(),
                responseMSGHTTP.transaction().getTerminal()
        );

        input.context().setResponse(responseMSGHTTP.transaction().getHexResponse());

        return new ProtocolFrame(ProtocolType.HTTP, responseMetadata, input.context());
    }

    @Nonnull
    private static String getReorderedTPDU(ConstructedHTTPMetadata metadata) {
        String TPDU = metadata.TPDU();

        int bytes = TPDU.length() / 2;

        if (bytes % 2 == 0)
            throw new IllegalArgumentException("TPDU must have an odd number of bytes");

        int half = (bytes - 1) / 2;

        int firstPartStart = 2;
        int firstPartEnd = 2 + (half * 2);

        int secondPartEnd = TPDU.length();

        return TPDU.substring(0, 2) +                    // first byte
                TPDU.substring(firstPartEnd, secondPartEnd) +
                TPDU.substring(firstPartStart, firstPartEnd);
    }
}
