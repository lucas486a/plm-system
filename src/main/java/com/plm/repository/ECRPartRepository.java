package com.plm.repository;

import com.plm.entity.ECRPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ECRPartRepository extends JpaRepository<ECRPart, Long> {

    List<ECRPart> findByEcrId(Long ecrId);

    Optional<ECRPart> findByEcrIdAndPartId(Long ecrId, Long partId);

    boolean existsByEcrIdAndPartId(Long ecrId, Long partId);

    void deleteByEcrIdAndPartId(Long ecrId, Long partId);
}
