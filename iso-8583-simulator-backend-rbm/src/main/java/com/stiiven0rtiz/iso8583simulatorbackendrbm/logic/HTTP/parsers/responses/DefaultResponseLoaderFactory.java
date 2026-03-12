package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.responses;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultResponseLoaderFactory {
    @Value("${http-config.defaultResponseFilePath}")
    private String defaultResponseFilePath;

    public HTTPResponseParser create(String responseFileName) {
        return new DefaultResponseLoader(defaultResponseFilePath + responseFileName);
    }
}
