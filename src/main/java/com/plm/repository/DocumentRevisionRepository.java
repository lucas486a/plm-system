package com.plm.repository;

import com.plm.entity.DocumentRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRevisionRepository extends JpaRepository<DocumentRevision, Long> {

    List<DocumentRevision> findByDocumentIdOrderByRevisionDesc(Long documentId);

    Optional<DocumentRevision> findByDocumentIdAndIsLatestRevisionTrue(Long documentId);

    Optional<DocumentRevision> findByDocumentIdAndRevisionAndIteration(Long documentId, String revision, Integer iteration);

    List<DocumentRevision> findByDocumentIdAndLifecycleState(Long documentId, String lifecycleState);
}
