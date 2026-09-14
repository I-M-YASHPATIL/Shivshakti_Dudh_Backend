package com.dairy.demo.repository;

import com.dairy.demo.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByFarmerIdOrderByEntryDateAscIdAsc(Long farmerId);
}
