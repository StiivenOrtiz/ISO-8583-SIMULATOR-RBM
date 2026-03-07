package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO;


// imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class to hold a framed message and a map of data element lengths.
 */
@Deprecated
@Getter @Setter
public class Iso8583FramedMessage {
    private ByteBuf message;
    private final Map<String, MSGLengths> dataElementsLengths = new TreeMap<>(
            Comparator.comparingInt(s -> Integer.parseInt(s.substring(1))));

    public Iso8583FramedMessage(byte[] message, Map<String, MSGLengths> dataElementsLengths) {
        this.message = Unpooled.copiedBuffer(message);
        this.dataElementsLengths.putAll(dataElementsLengths);
    }
}
