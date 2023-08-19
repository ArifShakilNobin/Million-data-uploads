package com.orangetoolz.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.orangetoolz.models.Lead;
import com.orangetoolz.repositories.LeadRepository;
import com.orangetoolz.utils.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class LeadService {

//    private static final int NUM_THREADS = 4; // Number of parallel threads

    private static final int NUM_CONSUMERS  = 4; // Number of parallel threads

    private final BlockingQueue<Lead> leadsQueue = new LinkedBlockingQueue<>();

    private final LeadRepository leadRepository;

//    public void processLeads(MultipartFile file) throws IOException {
//        List<Lead> leads = parseCSV(file);
//
//        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
//
//        List<Callable<Void>> tasks = new ArrayList<>();
//        for (Lead lead : leads) {
//            tasks.add(() -> {
//                leadRepository.save(lead);
//                return null;
//            });
//        }
//
//        try {
//            executorService.invokeAll(tasks);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        } finally {
//            executorService.shutdown();
//        }
//    }

    @Async
    public void processLeads(MultipartFile file) throws IOException {
        List<Lead> leads = CsvUtils.parseCsvFile(file);

        leadsQueue.addAll(leads);

        for (int i = 0; i < NUM_CONSUMERS; i++) {
            new Thread(new LeadConsumer()).start();
        }
    }

    private class LeadConsumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Lead lead = leadsQueue.take();
                    leadRepository.save(lead);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private List<Lead> parseCSV(MultipartFile file) throws IOException {
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
