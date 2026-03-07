package com.stiiven0rtiz.iso8583simulatorbackend.controllers;

import com.stiiven0rtiz.iso8583simulatorbackend.dto.CurrentInfoTransactions;
import com.stiiven0rtiz.iso8583simulatorbackend.dto.PageResponse;
import com.stiiven0rtiz.iso8583simulatorbackend.dto.TransactionDto;
import com.stiiven0rtiz.iso8583simulatorbackend.dto.TransactionsStatistics;
import com.stiiven0rtiz.iso8583simulatorbackend.services.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<TransactionDto> getTransactions(
            @RequestParam(required = false) String terminal,
            @RequestParam(required = false) String franchise,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String mti,

            @RequestParam(required = false) String status,
            @RequestParam(required = false) String responseCode,
            @RequestParam(required = false) String authCode,
            @RequestParam(required = false) String rrn,

            @RequestParam(required = false) Boolean responseCodeEmpty,
            @RequestParam(required = false) Boolean authCodeEmpty,
            @RequestParam(required = false) Boolean rrnEmpty,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTo,

            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,

            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return toPageResponse(service.getTransactions(
                terminal,
                franchise,
                transactionType,
                mti,
                status,
                responseCode,
                authCode,
                rrn,
                responseCodeEmpty,
                authCodeEmpty,
                rrnEmpty,
                dateFrom,
                dateTo,
                amountFrom,
                amountTo,
                search,
                page,
                size
        ));
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @GetMapping("/statistics")
    private TransactionsStatistics summary(@RequestParam(required = true) LocalDate from,
                                           @RequestParam(required = true) LocalDate to,
                                           @RequestParam(required = true) String groupBy) {
        return service.getStatistics(from, to, groupBy);
    }

    @GetMapping("/currentinfotransactions")
    public CurrentInfoTransactions getCurrentInfoTransactions() {
        return service.getCurrentInfo();
    }
}