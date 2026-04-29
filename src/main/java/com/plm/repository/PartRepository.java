package com.plm.repository;

import com.plm.entity.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByPartNumber(String partNumber);

    boolean existsByPartNumber(String partNumber);

    @Query("SELECT p FROM Part p WHERE " +
           "LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Part> searchParts(@Param("query") String query, Pageable pageable);
}
