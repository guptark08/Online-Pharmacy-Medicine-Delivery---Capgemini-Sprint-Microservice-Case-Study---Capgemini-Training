package org.sprint.catalogandprescription_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sprint.catalogandprescription_service.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findByIsActiveTrue();

	Optional<Category> findByIdAndIsActiveTrue(Long id);

	Optional<Category> findByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCase(String name);
}
