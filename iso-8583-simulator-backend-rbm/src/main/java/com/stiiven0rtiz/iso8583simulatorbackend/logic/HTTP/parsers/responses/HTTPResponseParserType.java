package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HTTPResponseParserType {
    HTTPResponsesParsers value();
}