package com.pharmacy.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacy.admin.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
