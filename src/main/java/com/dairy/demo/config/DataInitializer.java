package com.dairy.demo.config;

import com.dairy.demo.repository.FatRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FatRateRepository fatRateRepository;

    @Override
    public void run(String... args) {
        long count = fatRateRepository.count();
        log.info("Fat rates in DB: {}. Seeding disabled — manage via UI.", count);
    }
}