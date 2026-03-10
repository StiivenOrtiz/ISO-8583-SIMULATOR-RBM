package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

public sealed interface ResponseMSG
        permits ResponseMSGHTTP, ResponseMSGIso8583 {
}