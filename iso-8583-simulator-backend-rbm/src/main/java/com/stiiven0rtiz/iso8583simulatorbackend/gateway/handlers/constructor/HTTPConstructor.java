package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.SupportsProtocol;

@SupportsProtocol(ProtocolType.HTTP)
public class HTTPConstructor implements ProtocolFrameConstructor {

    @Override
    public ProtocolFrame constructMSG(ProtocolFrame input) {
        DecodedHTTPMetadata metadata = (DecodedHTTPMetadata) input.metadata();
        byte[] rawMessage = metadata.rawMessage();

        
    }
}
