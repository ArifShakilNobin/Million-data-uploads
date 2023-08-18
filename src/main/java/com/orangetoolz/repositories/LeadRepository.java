package com.orangetoolz.repositories;

import com.orangetoolz.models.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    // Define custom query methods if needed
}
