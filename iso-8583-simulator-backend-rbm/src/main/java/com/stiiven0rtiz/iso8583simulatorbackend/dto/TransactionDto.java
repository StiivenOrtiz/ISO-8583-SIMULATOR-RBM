package com.stiiven0rtiz.iso8583simulatorbackend.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * TransactionLiveDto.java
 * <p>
 * Data Transfer Object for live transaction data over WebSocket.
 *
 * @version 1.2
 */
public record TransactionDto(
        String id,
        String uuid,
        String txTimestamp,
        String terminal,
        BigDecimal amount,
        String franchise,
        String franchiseLogo,
        String transactionType,
        String mti,
        String status,
        String responseCode,
        String authCode,
        String rrn,
        String bitmapPrimary,
        String bitmapSecondary,
        Map<String, String> requestDataElements,
        Map<String, String> responseDataElements,
        String hexRequest,
        String hexResponse
) {}
