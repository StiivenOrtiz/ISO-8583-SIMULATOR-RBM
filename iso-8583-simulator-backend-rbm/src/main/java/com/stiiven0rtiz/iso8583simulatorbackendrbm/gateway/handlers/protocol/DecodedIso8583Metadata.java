package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.POJO.MSGLengths;
import io.netty.buffer.ByteBuf;

import java.util.Map;

public record DecodedIso8583Metadata(
        ByteBuf message,
        Map<Integer, MSGLengths> dataElementsLengths
) implements ProtocolMetadata {
    public DecodedIso8583Metadata {
        dataElementsLengths = Map.copyOf(dataElementsLengths);
    }
}
