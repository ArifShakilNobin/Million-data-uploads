package com.orangetoolz.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.orangetoolz.models.Lead;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static List<Lead> parseCsvFile(MultipartFile file) throws IOException {
        List<Lead> leads = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length >= 3) {
                    Lead lead = new Lead();
                    lead.setFirstName(line[0]);
                    lead.setLastName(line[1]);
                    lead.setEmail(line[2]);
                    leads.add(lead);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        return leads;
    }
}