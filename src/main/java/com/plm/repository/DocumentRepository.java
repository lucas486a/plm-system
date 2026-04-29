package com.plm.repository;

import com.plm.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);
}
