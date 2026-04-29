package com.plm.repository;

import com.plm.entity.ECR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ECRRepository extends JpaRepository<ECR, Long> {

    Optional<ECR> findByEcrNumber(String ecrNumber);

    boolean existsByEcrNumber(String ecrNumber);

    List<ECR> findByStatus(String status);

    List<ECR> findByPriority(String priority);

    List<ECR> findByAssignedToId(Long userId);
}
