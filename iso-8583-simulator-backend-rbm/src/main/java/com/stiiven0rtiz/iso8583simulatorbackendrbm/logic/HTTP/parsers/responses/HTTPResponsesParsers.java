package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.responses;

public enum HTTPResponsesParsers {
    NOT_MAPPED("not_mapped"),
    NO_RESPONSE("no_response"),
    UNKNOWN("unknown_response"),
    ;

    private final String name;

    HTTPResponsesParsers(String name) { this.name = name; }

    public static HTTPResponsesParsers from(String value) {
        for (HTTPResponsesParsers p : values())
            if (p.name.equals(value)) return p;

        return UNKNOWN;
    }
}