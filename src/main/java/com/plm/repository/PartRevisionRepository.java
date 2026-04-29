package com.plm.repository;

import com.plm.entity.PartRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartRevisionRepository extends JpaRepository<PartRevision, Long> {

    List<PartRevision> findByPartIdOrderByRevisionDesc(Long partId);

    Optional<PartRevision> findByPartIdAndIsLatestRevisionTrue(Long partId);

    Optional<PartRevision> findByPartIdAndRevisionAndIteration(Long partId, String revision, Integer iteration);

    List<PartRevision> findByPartIdAndLifecycleState(Long partId, String lifecycleState);
}
