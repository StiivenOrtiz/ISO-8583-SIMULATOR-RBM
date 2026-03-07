package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.response;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;

public interface ProtocolResponse {

    ProtocolFrame response(ProtocolFrame input) throws Exception;

}
