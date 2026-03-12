package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;

public record ResponseMSGIso8583(
        Iso8583Msg responseMsg
) implements ResponseMSG {
}
