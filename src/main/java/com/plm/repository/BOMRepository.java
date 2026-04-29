package com.plm.repository;

import com.plm.entity.BOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BOMRepository extends JpaRepository<BOM, Long> {

    List<BOM> findByAssemblyId(Long assemblyId);

    List<BOM> findByStatus(String status);

    List<BOM> findByAssemblyIdAndStatus(Long assemblyId, String status);
}
