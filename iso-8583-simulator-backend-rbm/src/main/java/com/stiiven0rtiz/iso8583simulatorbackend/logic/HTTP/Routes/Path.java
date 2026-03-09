package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.Routes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Path {
    private String method;
    private String path;
    private String requestSchema;
    private String responseSchema;
}