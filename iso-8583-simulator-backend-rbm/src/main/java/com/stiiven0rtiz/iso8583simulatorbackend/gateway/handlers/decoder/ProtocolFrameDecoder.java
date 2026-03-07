package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.decoder;

import io.netty.buffer.ByteBuf;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;

public interface ProtocolFrameDecoder {

    /**
     * @return ProtocolFrame when a full frame is available, null otherwise
     */
    ProtocolFrame decode(ByteBuf in);
}

