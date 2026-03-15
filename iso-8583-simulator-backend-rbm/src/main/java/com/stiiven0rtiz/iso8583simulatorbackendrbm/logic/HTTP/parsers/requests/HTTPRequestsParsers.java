package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests;

public enum HTTPRequestsParsers {
    NOT_MAPPED("not_mapped"),
    DIGITAL_VOUCHER("digital_voucher"),
    DIGITAL_VOUCHER_SIGNATURE("digital_voucher_signature"),
    ;

    private final String name;

    HTTPRequestsParsers(String name) {
        this.name = name;
    }

    public static HTTPRequestsParsers from(String value) {
        for (HTTPRequestsParsers p : values())
            if (p.name.equals(value)) return p;

        return NOT_MAPPED;
    }
}
