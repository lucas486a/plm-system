package com.plm.repository;

import com.plm.entity.BOMItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BOMItemRepository extends JpaRepository<BOMItem, Long> {

    List<BOMItem> findByBomId(Long bomId);

    List<BOMItem> findByPartRevisionId(Long partRevisionId);

    List<BOMItem> findByBomIdAndIsMounted(Long bomId, Boolean isMounted);
}
