package com.plm.repository;

import com.plm.entity.ECOApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ECOApprovalRepository extends JpaRepository<ECOApproval, Long> {

    List<ECOApproval> findByEcoId(Long ecoId);

    List<ECOApproval> findByEcoIdAndStage(Long ecoId, String stage);

    List<ECOApproval> findByApproverId(Long approverId);
}
