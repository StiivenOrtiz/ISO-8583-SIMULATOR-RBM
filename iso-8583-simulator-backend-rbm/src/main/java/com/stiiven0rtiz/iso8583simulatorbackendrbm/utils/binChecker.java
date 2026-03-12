package com.stiiven0rtiz.iso8583simulatorbackendrbm.utils;

import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Component
public class binChecker {
    Logger logger = LoggerFactory.getLogger(binChecker.class);

    @Value("${bin-checker.binlist.filepath}")
    private String binfilePath;

    @Value("${bin-checker.franchises_brands.filepath}")
    private String franchisesBrandsFilePath;

    @Value("${bin-checker.types_card.filepath}")
    private String typeCardsFilePath;

    private final Map<String, Map<String, String>> binListMap = new HashMap<>();
    private final Map<String, String> franchisesBrandsMap = new HashMap<>();
    private final Map<String, String> typeCardsMap = new HashMap<>();

    /**
     * This method is called after the bean's properties have been set.
     * It loads the CSV files into memory.
     */
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing BIN CSV data load...");

            loadBinList();
            loadFranchisesBrands();
            loadTypesCard();

            logger.info("BIN CSV data loaded successfully.");

            logger.info("BIN List Map Size: {}", binListMap.size());
            logger.info("Franchises Brands Map Size: {}", franchisesBrandsMap.size());
            logger.info("Type Cards Map Size: {}", typeCardsMap.size());
        } catch (IOException e) {
            logger.error("Error loading BIN CSV data", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to load BIN CSV data", e);
        }
    }

    /**
     * Load the bin_list.csv file.
     * @throws IOException If a reading error occurs.
     */
    private void loadBinList() throws IOException {
        binfilePath = "settings/bin_list/bin_list.csv";
        try (Reader in = new FileReader(binfilePath);
             CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("bin", "id_franchise_brand", "type", "country_code")
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : parser) {
                String bin = record.get("bin");

                // Create a sub-map for the attributes of each bin.
                Map<String, String> attributes = new HashMap<>();
                attributes.put("id_franchise_brand", record.get("id_franchise_brand"));
                attributes.put("type", record.get("type"));
                attributes.put("country_code", record.get("country_code"));

                // The main map's key is 'bin' and the value is the attributes sub-map.
                binListMap.put(bin, attributes);
            }
        }
    }

    /**
     * Load the franchises_brands.csv file.
     * @throws IOException If a reading error occurs.
     */
    private void loadFranchisesBrands() throws IOException {
        franchisesBrandsFilePath = "settings/bin_list/franchises_brands.csv";
        try (Reader in = new FileReader(franchisesBrandsFilePath);
             CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("id_franchise_brand", "franchise_brand")
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : parser) {
                String id = record.get("id_franchise_brand");
                String name = record.get("franchise_brand");

                // The main map's key is 'id_franchise_brand' and the value is 'franchise_brand'.
                franchisesBrandsMap.put(id, name);
            }
        }
    }

    /**
     * Load the types_card.csv file.
     * @throws IOException If a reading error occurs.
     */
    private void loadTypesCard() throws IOException {
        typeCardsFilePath = "settings/bin_list/types_card.csv";
        try (Reader in = new FileReader(typeCardsFilePath);
             CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("id_type", "type")
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : parser) {
                String id = record.get("id_type");
                String type = record.get("type");

                // The main map's key is 'id_type' and the value is 'type'.
                typeCardsMap.put(id, type);
            }
        }
    }

    public String getFranchiseByBIN(String bin) {
        Map<String, String> binAttributes = binListMap.get(bin);
        if (binAttributes != null) {
            String idFranchiseBrand = binAttributes.get("id_franchise_brand");
            return franchisesBrandsMap.get(idFranchiseBrand);
        }
        return "unknown";
    }

    public String getTypeByBIN(String bin) {
        Map<String, String> binAttributes = binListMap.get(bin);
        if (binAttributes != null) {
            String typeId = binAttributes.get("type");
            return typeCardsMap.get(typeId);
        }
        return "unknown";
    }

    public String getCountryCodeByBIN(String bin) {
        Map<String, String> binAttributes = binListMap.get(bin);
        if (binAttributes != null) {
            return binAttributes.get("country_code");
        }
        return "unknown";
    }
}
