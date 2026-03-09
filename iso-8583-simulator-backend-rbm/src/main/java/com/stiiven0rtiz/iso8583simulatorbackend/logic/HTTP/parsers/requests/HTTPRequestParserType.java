package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HTTPRequestParserType {
    HTTPRequestsParsers value();
}
