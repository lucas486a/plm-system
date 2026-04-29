package com.plm.repository;

import com.plm.entity.ECO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ECORepository extends JpaRepository<ECO, Long> {

    Optional<ECO> findByEcoNumber(String ecoNumber);

    boolean existsByEcoNumber(String ecoNumber);

    List<ECO> findByStatus(String status);

    List<ECO> findByType(String type);

    List<ECO> findByEcrId(Long ecrId);
}
