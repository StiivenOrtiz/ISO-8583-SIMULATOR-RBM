package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.constructor;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;

public interface ProtocolFrameConstructor {

    ProtocolFrame constructMSG(ProtocolFrame input) throws Exception;

}
