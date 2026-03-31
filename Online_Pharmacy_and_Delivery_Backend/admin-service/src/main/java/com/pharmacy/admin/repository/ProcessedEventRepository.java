package com.pharmacy.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacy.admin.entity.ProcessedEvent;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}