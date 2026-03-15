package com.stiiven0rtiz.iso8583simulatorbackendrbm.services;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.config.txsConfig.TransactionNamesConfig;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.dto.BucketSeries;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.dto.CurrentInfoTransactions;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.dto.TransactionDto;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.dto.TransactionsStatistics;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.events.TransactionCSavedEvent;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.events.TransactionConstructed;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.events.TransactionSavedEvent;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.Iso8583Msg;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message.ValidatedVariable;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.model.digitalvoucher.ParsedDigitalVoucherField;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.TransactionNamesLoader;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.mapper.TransactionMapper;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.*;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.repositories.TransactionRepository;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.utils.IsoUtils;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.utils.binChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * TransactionService.java
 * <p>
 * This service class provides methods to manage Transaction entities,
 * including saving transactions and handling errors.
 *
 * @version 1.5
 */
@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    String thisId = toString().substring(toString().indexOf("@"));

    private final TransactionRepository repository;
    private final Iso8583FieldService iso8583FieldService;
    private final ApplicationEventPublisher publisher;
    private final binChecker binChecker;
    private final TransactionNamesLoader transactionNamesLoader;

    public TransactionService(TransactionRepository repository, Iso8583FieldService iso8583FieldService, ApplicationEventPublisher publisher, binChecker binChecker, TransactionNamesLoader transactionNamesLoader) {
        this.repository = repository;
        this.iso8583FieldService = iso8583FieldService;
        this.publisher = publisher;
        this.binChecker = binChecker;
        this.transactionNamesLoader = transactionNamesLoader;
    }

    public List<Transaction> getAll() {
        return repository.findAll();
    }

    public TransactionsStatistics getStatistics(
            LocalDate from,
            LocalDate to,
            String groupBy
    ) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        long total = repository.countByRange(start, end);

        boolean singleDay = from.equals(to);

        List<BucketSeries> series;

        if (singleDay && "hour".equalsIgnoreCase(groupBy)) {

            series = repository.countGroupedByHour(start, end)
                    .stream()
                    .map(obj -> {
                        Integer hour = (Integer) obj[0];
                        Long count = (Long) obj[1];
                        return new BucketSeries(
                                String.format("%02d:00", hour),
                                count
                        );
                    })
                    .toList();

        } else {

            series = repository.countGroupedByDay(start, end)
                    .stream()
                    .map(obj -> {
                        Object date = obj[0];
                        Long count = (Long) obj[1];
                        return new BucketSeries(
                                date.toString(),
                                count
                        );
                    })
                    .toList();
        }

        return new TransactionsStatistics(total, series);
    }

    public Page<TransactionDto> getTransactions(
            String terminal,
            String franchise,
            String transactionType,
            String mti,
            String status,
            String responseCode,
            String authCode,
            String rrn,
            Boolean responseCodeEmpty,
            Boolean authCodeEmpty,
            Boolean rrnEmpty,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            String searchText,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("txTimestamp").descending()
        );

        Specification<Transaction> spec =
                TransactionSpecification.filter(
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
                        searchText
                );

        return repository.findAll(spec, pageable).map(TransactionMapper::toDto);
    }

    public void saveError(String rawMessageRequest) {
        logger.debug("SaveError rawMessageRequest={}", rawMessageRequest);
        saveError(rawMessageRequest, ProtocolType.UNKNOWN);
    }

    public void saveError(String rawMessageRequest, ProtocolType protocolType) {
        logger.debug("SaveError rawMessageRequest={}, protocolType={}", rawMessageRequest, protocolType);
        saveError(rawMessageRequest, protocolType, LocalDateTime.now());
    }


    public void saveError(String rawMessageRequest, ProtocolType protocolType, LocalDateTime time) {
        logger.debug("SaveError rawMessageRequest={}, time={}", rawMessageRequest, time);

        Transaction tx = new Transaction();
        tx.setUuid(UUID.randomUUID().toString());

        tx.setProtocol(protocolType.name());
        tx.setTxTimestamp(time);
        tx.setStatus(MessageStatus.failed);
        tx.setResponseCode("N/A");

        tx.setHexRequest(rawMessageRequest);

        tx.setReceivedAt(time);

        tx = repository.save(tx);

        publisher.publishEvent(new TransactionSavedEvent(tx));
    }


    private Transaction buildTransaction(Iso8583Msg isoMsg, LocalDateTime receivedAt, LocalDateTime constructedAt) {
        logger.debug("SaveConstruction isoMsg={}, receivedAt={}", isoMsg, receivedAt);

        Transaction tx = new Transaction();
        tx.setUuid(UUID.randomUUID().toString());
        tx.setProtocol(ProtocolType.ISO8583.name());

        tx.setTerminal((String) isoMsg.getDataElement("P41"));

        if (isoMsg.getDataElement("P4") != null)
            tx.setAmount(new BigDecimal((String) isoMsg.getDataElement("P4")).movePointLeft(2));

        Object track2 = isoMsg.getDataElement("P35");

        if (track2 != null)
            tx.setFranchise(binChecker.getFranchiseByBIN(
                    IsoUtils.getBinFromTrack2((String) track2)));
        else
            tx.setFranchise("none");

        String id = isoMsg.getMTI().getValue().replace(" ", "") + isoMsg.getDataElement("P3");
        TransactionNamesConfig transactionType = transactionNamesLoader.getTransactionNames();

        if (transactionType != null && transactionType.getNames() != null) {
            String name = transactionType.getNames().get(id);
            tx.setTransactionType(Objects.requireNonNullElse(name, "unknown"));
        } else {
            tx.setTransactionType("unknown");
        }

        if (isoMsg.getMTI() != null)
            tx.setMti(isoMsg.getMTI().getValue().replace(" ", ""));
        tx.setStatus(MessageStatus.pending);

        if (isoMsg.getBitmap() != null)
            tx.setBitmapPrimary(isoMsg.getBitmap().getValue().replace(" ", ""));

        Object secondaryBitmap = isoMsg.getDataElement("P1");

        if (secondaryBitmap != null) {
            logger.debug("{} - Secondary bitmap found: {}", thisId, secondaryBitmap);
            tx.setBitmapSecondary(((String) secondaryBitmap).replace(" ", ""));
        }

        tx.setHexRequest(isoMsg.getRawData());
        tx.setReceivedAt(receivedAt);
        tx.setConstructedAt(constructedAt);

        generateISOFields(tx, isoMsg, MessageType.REQUEST);

        return tx;
    }

    public Optional<Transaction> findLastHTTPTransaction(
            String terminal,
            String fieldId1,
            String fieldValue1,
            String fieldId2,
            String fieldValue2
    ) {
        List<Transaction> result =
                repository.findLatestHTTPTransactionByTwoDVFields(
                        terminal,
                        ProtocolType.HTTP.name(),
                        fieldId1,
                        fieldValue1,
                        fieldId2,
                        fieldValue2
                );

        return result.stream().findFirst();
    }


    /**
     * Save a new construction transaction based on the received ISO 8583 message.
     *
     * @param isoMsg     message received
     * @param receivedAt timestamp when the message was received
     * @return the ID of the saved transaction
     */
    public Long saveISOConstruction(Iso8583Msg isoMsg, LocalDateTime receivedAt, LocalDateTime constructedAt) {

        Transaction tx = buildTransaction(isoMsg, receivedAt, constructedAt);

        tx = repository.save(tx);

        publisher.publishEvent(new TransactionSavedEvent(tx));

        return tx.getId();
    }

    public Transaction generateConstruction(Iso8583Msg isoMsg, LocalDateTime receivedAt, LocalDateTime constructedAt) {

        Transaction tx = buildTransaction(isoMsg, receivedAt, constructedAt);

        publisher.publishEvent(new TransactionConstructed(tx));

        return tx;
    }

    public static void generateVDFields(Transaction tx, Map<String, ParsedDigitalVoucherField> fields, MessageType type) {

        for (Map.Entry<String, ParsedDigitalVoucherField> entry : fields.entrySet()) {
            DigitalVoucherField dvf = new DigitalVoucherField();
            dvf.setMessageType(type);
            dvf.setFieldId(entry.getKey());
            dvf.setFieldValue(entry.getValue().getValue());
            dvf.setFieldLength(entry.getValue().getLength());

            tx.addDigitalVoucherField(dvf);
        }
    }

    private void generateISOFields(Transaction tx, Iso8583Msg isoMsg, MessageType type) {

        for (Map.Entry<String, ValidatedVariable<Object>> field : isoMsg.getDataElements().entrySet()) {

            Iso8583Field isoField = new Iso8583Field();
            isoField.setMessageType(type);
            isoField.setFieldId(field.getKey());

            Object value = field.getValue().getValue();
            isoField.setFieldValue(value != null ? value.toString() : null);

            tx.addField(isoField);
        }
    }

    public void setResponseAttributes(Transaction tx, int artificialDelay, Iso8583Msg responseMsg,
                                      LocalDateTime responseTime, LocalDateTime processedAt) {
        tx.setTxTimestamp(responseTime);

        tx.setStatus(MessageStatus.success);
        tx.setResponseCode((String) responseMsg.getDataElement("P39"));
        tx.setRrn((String) responseMsg.getDataElement("P37"));
        tx.setAuthCode((String) responseMsg.getDataElement("P38"));

        tx.setHexResponse(responseMsg.getRawData());

        tx.setArtificialDelay(artificialDelay);

        tx.setProcessedAt(processedAt);
        tx.setResponseSentAt(responseTime);
    }

    /**
     * Save the response for a given transaction ID with artificial delay and response message.
     *
     * @param id              the transaction ID
     * @param artificialDelay the artificial delay applied
     * @param responseMsg     the response ISO 8583 message
     */
    public void saveISOResponse(Long id, int artificialDelay, Iso8583Msg responseMsg,
                                LocalDateTime responseTime, LocalDateTime processedAt) {
        logger.debug("SaveResponse Id={}, artificialDelay={}, responseMsg={}", id, artificialDelay, responseMsg);

        Transaction tx = repository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));

        setResponseAttributes(tx, artificialDelay, responseMsg, responseTime, processedAt);

        tx = repository.save(tx);

        saveISOFields(tx, responseMsg);

        publisher.publishEvent(new TransactionSavedEvent(tx));
    }


    public void saveISOResponse(Transaction tx, int artificialDelay, Iso8583Msg responseMsg,
                                LocalDateTime responseTime, LocalDateTime processedAt) {
        logger.debug("SaveResponse UUID={}, artificialDelay={}, responseMsg={}", tx.getUuid(), artificialDelay, responseMsg);

        setResponseAttributes(tx, artificialDelay, responseMsg, responseTime, processedAt);
        generateISOFields(tx, responseMsg, MessageType.RESPONSE);

        tx = repository.save(tx);

        publisher.publishEvent(new TransactionCSavedEvent(tx));
    }

    public void saveErrorResponse(Long id) {
        logger.debug("SaveErrorResponse Id={}", id);

        Transaction tx = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setStatus(MessageStatus.failed);
        tx.setResponseCode("N/A");

        tx = repository.save(tx);

        publisher.publishEvent(new TransactionSavedEvent(tx));
    }


    /**
     * Save ISO 8583 fields for a given transaction.
     *
     * @param tx     the transaction
     * @param isoMsg the ISO 8583 message
     */
    private void saveISOFields(Transaction tx, Iso8583Msg isoMsg) {
        logger.debug("SaveFields tx={} isoMsg={}", tx, isoMsg);
        for (Map.Entry<String, ValidatedVariable<Object>> field : isoMsg.getDataElements().entrySet()) {
            Iso8583Field isoField = new Iso8583Field();
            isoField.setTransaction(tx);
            isoField.setMessageType(MessageType.RESPONSE);
            isoField.setFieldId(field.getKey());

            Object value = field.getValue().getValue();
            if (value instanceof String)
                isoField.setFieldValue(((String) value).replace(" ", ""));
            else if (value instanceof Integer || value instanceof Long || value instanceof Double) {
                isoField.setFieldValue(String.valueOf(value));
            } else if (value instanceof Boolean)
                isoField.setFieldValue(((Boolean) value) ? "1" : "0");
            else isoField.setFieldValue(value.toString());

            iso8583FieldService.saveField(isoField);
        }
    }

    public CurrentInfoTransactions getCurrentInfo() {
        List<String> terminals = repository.findDistinctTerminals();
        List<String> franchises = repository.findDistinctFranchises();
        List<String> transactionTypes = repository.findDistinctTransactionTypes();
        List<String> mtis = repository.findDistinctMTIs();
        List<String> statusValues = repository.findDistinctStatusValues();

        return new CurrentInfoTransactions(
                terminals,
                franchises,
                transactionTypes,
                mtis,
                statusValues
        );
    }

    public void saveErrorT(Transaction tx, LocalDateTime constructionCompletedAt) {
        logger.debug("SaveError tx UUID={}, constructionCompletedAt={}", tx.getUuid(), constructionCompletedAt);

        tx.setTxTimestamp(constructionCompletedAt);
        tx.setStatus(MessageStatus.failed);
        tx.setResponseCode("N/A");
        tx.setReceivedAt(constructionCompletedAt);

        tx = repository.save(tx);

        publisher.publishEvent(new TransactionCSavedEvent(tx));
    }

    public Transaction generateHTTPConstruction(Transaction transaction, LocalDateTime receivedAt, LocalDateTime constructedAt) {
        logger.debug("GenerateHTTPConstruction transaction={}, receivedAt={}", transaction, receivedAt);

        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setProtocol(ProtocolType.HTTP.name());
        transaction.setStatus(MessageStatus.pending);
        transaction.setReceivedAt(receivedAt);
        transaction.setConstructedAt(constructedAt);

        publisher.publishEvent(new TransactionConstructed(transaction));

        return transaction;
    }

    public void saveHTTPResponse(Transaction tx, Transaction responseParser, int artificialDelay, String rawData, LocalDateTime respondedAt, LocalDateTime processedAt) {
        logger.debug("SaveHTTPResponse tx UUID={}, responseParer={}, artificialDelay={}, rawData={}", tx.getUuid(), responseParser, artificialDelay, rawData);

        tx.setTxTimestamp(respondedAt);
        tx.setStatus(MessageStatus.success);

        tx.setHexResponse(rawData);
        tx.setArtificialDelay(artificialDelay);
        tx.setProcessedAt(processedAt);
        tx.setResponseSentAt(respondedAt);

        tx = repository.save(tx);

        publisher.publishEvent(new TransactionCSavedEvent(tx));

        validateDVSignature(tx);
    }


    private void validateDVSignature(Transaction tx) {
        logger.debug("{} - Validating signature", thisId);

        String signatureId = "signature";
        String receiptId = "receipt";

        // get dvfield receipt
        DigitalVoucherField dvFieldReceipt = findField(tx, receiptId);
        if (dvFieldReceipt == null) return;

        logger.debug("{} - Found receipt field: {}", thisId, dvFieldReceipt);

        Transaction transaction = findLastHTTPTransaction(
                tx.getTerminal(),
                "65", dvFieldReceipt.getFieldValue(),
                "08", "1"
        ).orElse(null);

        if (transaction == null) return;

        logger.debug("{} - Found transaction: {}", thisId, transaction);

        DigitalVoucherField dvFieldSignature = findField(tx, signatureId);

        if (dvFieldSignature == null) return;

        logger.debug("{} - Found signature: {}", thisId, dvFieldSignature);

        String signature = dvFieldSignature.getFieldValue();
        DigitalVoucherField package_ = findField(tx, "package");
        DigitalVoucherField targetSignatureField = findField(transaction, signatureId);


        if (package_== null) return;

        boolean change = false;

        if (targetSignatureField == null) {
            logger.debug("{} - Missing target signature: {}", thisId, signature);

            DigitalVoucherField newSignature = new DigitalVoucherField();
            newSignature.setFieldId(signatureId);
            newSignature.setFieldValue(signature);
            newSignature.setFieldLength(signature.length());
            newSignature.setMessageType(MessageType.REQUEST);
            transaction.addDigitalVoucherField(newSignature);


            DigitalVoucherField newPackage = new DigitalVoucherField();
            newPackage.setFieldId(package_.getFieldId());
            newPackage.setFieldValue(package_.getFieldValue());
            newPackage.setFieldLength(package_.getFieldLength());
            newPackage.setMessageType(MessageType.REQUEST);
            transaction.addDigitalVoucherField(newPackage);

            DigitalVoucherField packages = findField(tx, "packages");

            DigitalVoucherField newPackages = new DigitalVoucherField();
            newPackages.setFieldId(packages.getFieldId());
            newPackages.setFieldValue(packages.getFieldValue());
            newPackages.setFieldLength(packages.getFieldLength());
            newPackages.setMessageType(MessageType.REQUEST);
            transaction.addDigitalVoucherField(newPackages);

            change = true;
        } else{
            DigitalVoucherField packageTx = findField(transaction, "package");
            DigitalVoucherField packagesTx = findField(transaction, "packages");

            if (packageTx == null || packagesTx == null) return;

            int packagesValue = Integer.parseInt(packagesTx.getFieldValue());
            int packageValue = Integer.parseInt(packageTx.getFieldValue());

            logger.debug("{} - Validating packages and package: {}, {}", thisId, packagesValue,  packageValue);

            if (packagesValue != packageValue) { // store more packages signature
                logger.debug("{} - Found target signature: {}", thisId, targetSignatureField);
                targetSignatureField.setFieldValue(targetSignatureField.getFieldValue() + signature);
                targetSignatureField.setFieldLength(targetSignatureField.getFieldLength() + signature.length());
                packageTx.setFieldValue(package_.getFieldValue());
                packageTx.setFieldLength(package_.getFieldLength());
                change = true;
            } else
                logger.debug("{} - All packages are loaded: {}, dont save save nothing", thisId, targetSignatureField);
        }

        // if no change, do not save
        if (!change) return;

        // save changes
        transaction = repository.save(transaction);
        logger.debug("{} - saved transaction: {}", thisId, transaction);

        publisher.publishEvent(new TransactionCSavedEvent(transaction));
        logger.debug("{} - published event for transaction: {}", thisId, transaction);
    }

    private DigitalVoucherField findField(Transaction tx, String id){
        return tx.getDigitalVoucherFields().stream()
                .filter(f -> id.equals(f.getFieldId()))
                .findFirst()
                .orElse(null);
    }

}
