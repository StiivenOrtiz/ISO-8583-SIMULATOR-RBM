package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.response;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.*;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.config.IsoFieldsData;
import com.stiiven0rtiz.iso8583simulatorbackend.iso.message.Iso8583Msg;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ISOResponseLoader;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.ResponseCodeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stiiven0rtiz.iso8583simulatorbackend.utils.IsoUtils.iso8584MSGToHEXString;

@SupportsProtocol(ProtocolType.ISO8583)
public class Iso8583Response implements ProtocolResponse {
    private static final Logger logger = LoggerFactory.getLogger(Iso8583Response.class);
    String thisId = toString().substring(toString().indexOf("@"));

    ISOResponseLoader isoResponseLoader;
    IsoFieldsData isoFieldsData;
    ResponseCodeLoader responseCodeLoader;

    public Iso8583Response(IsoFieldsData isoFieldsData,
                           ISOResponseLoader isoResponseLoader,
                           ResponseCodeLoader responseCodeLoader) {
        this.isoFieldsData = isoFieldsData;
        this.isoResponseLoader = isoResponseLoader;
        this.responseCodeLoader = responseCodeLoader;
    }

    @Override
    public ProtocolFrame response(ProtocolFrame input) throws Exception {
        ConstructedIso8583Metadata metadata = (ConstructedIso8583Metadata) input.metadata();
        logger.debug("{} - Received ISO 8583 MSG.", thisId);

        Iso8583Msg isoC = metadata.iso8583Msg();
        Iso8583Msg responseMsg = isoResponseLoader.buildResponse(isoC);
        responseCodeLoader.validateResponseCode(responseMsg);

        responseMsg.setRawData(iso8584MSGToHEXString(isoFieldsData, responseMsg)
                .replace(" ", ""));

        String terminalId = (String) isoC.getDataElement("P41");

        ProtocolMetadata responseMetadata = new ResponseMetadata(
                new ResponseMSGIso8583(responseMsg),
                responseMsg.getRawData(),
                terminalId
        );

        input.context().setResponse(responseMsg.getRawData());

        return new ProtocolFrame(ProtocolType.ISO8583, responseMetadata, input.context());
    }
}