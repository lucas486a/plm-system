package com.plm.repository;

import com.plm.entity.Assembly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssemblyRepository extends JpaRepository<Assembly, Long> {

    Optional<Assembly> findByPartNumber(String partNumber);

    boolean existsByPartNumber(String partNumber);
}
