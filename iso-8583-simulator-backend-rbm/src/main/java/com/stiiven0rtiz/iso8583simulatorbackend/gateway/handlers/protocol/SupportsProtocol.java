package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportsProtocol {
    ProtocolType value();
}
