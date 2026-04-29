package com.plm.service;

import com.plm.config.CacheConfig;
import com.plm.dto.PartDTO;
import com.plm.dto.PartRevisionDTO;
import com.plm.entity.Part;
import com.plm.entity.PartRevision;
import com.plm.mapper.PartMapper;
import com.plm.mapper.PartRevisionMapper;
import com.plm.repository.PartRepository;
import com.plm.repository.PartRevisionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final PartRevisionRepository partRevisionRepository;
    private final PartMapper partMapper;
    private final PartRevisionMapper partRevisionMapper;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PARTS_CACHE, allEntries = true)
    public PartDTO createPart(PartDTO partDTO) {
        log.info("Creating part with number: {}", partDTO.getPartNumber());

        if (partRepository.existsByPartNumber(partDTO.getPartNumber())) {
            throw new IllegalArgumentException("Part with number " + partDTO.getPartNumber() + " already exists");
        }

        Part part = partMapper.toEntity(partDTO);
        Part savedPart = partRepository.save(part);
        log.info("Created part with id: {}", savedPart.getId());
        return partMapper.toDto(savedPart);
    }

    @Override
    @Cacheable(value = CacheConfig.PARTS_CACHE, key = "#id")
    public PartDTO getPartById(Long id) {
        log.debug("Fetching part by id: {}", id);
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found with id: " + id));
        return partMapper.toDto(part);
    }

    @Override
    @Cacheable(value = CacheConfig.PARTS_CACHE, key = "#partNumber")
    public PartDTO getPartByNumber(String partNumber) {
        log.debug("Fetching part by number: {}", partNumber);
        Part part = partRepository.findByPartNumber(partNumber)
                .orElseThrow(() -> new EntityNotFoundException("Part not found with number: " + partNumber));
        return partMapper.toDto(part);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PARTS_CACHE, allEntries = true)
    public PartDTO updatePart(Long id, PartDTO partDTO) {
        log.info("Updating part with id: {}", id);

        Part existingPart = partRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found with id: " + id));

        // Check if partNumber is being changed and if new number already exists
        if (!existingPart.getPartNumber().equals(partDTO.getPartNumber()) &&
            partRepository.existsByPartNumber(partDTO.getPartNumber())) {
            throw new IllegalArgumentException("Part with number " + partDTO.getPartNumber() + " already exists");
        }

        existingPart.setPartNumber(partDTO.getPartNumber());
        existingPart.setName(partDTO.getName());
        existingPart.setDescription(partDTO.getDescription());
        existingPart.setPartType(partDTO.getPartType());
        existingPart.setDefaultUnit(partDTO.getDefaultUnit());

        try {
            Part updatedPart = partRepository.save(existingPart);
            log.info("Updated part with id: {}", updatedPart.getId());
            return partMapper.toDto(updatedPart);
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock conflict for part id: {}", id);
            throw new OptimisticLockException("Part was modified by another user. Please refresh and try again.");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.PARTS_CACHE, allEntries = true)
    public void deletePart(Long id) {
        log.info("Deleting part with id: {}", id);

        Part part = partRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Part not found with id: " + id));

        try {
            partRepository.delete(part);
            log.info("Deleted part with id: {}", id);
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock conflict for part id: {}", id);
            throw new OptimisticLockException("Part was modified by another user. Please refresh and try again.");
        }
    }

    @Override
    public Page<PartDTO> listParts(Pageable pageable) {
        log.debug("Listing parts with pagination: {}", pageable);
        return partRepository.findAll(pageable)
                .map(partMapper::toDto);
    }

    @Override
    public Page<PartDTO> searchParts(String query, Pageable pageable) {
        log.debug("Searching parts with query: {}, pagination: {}", query, pageable);
        return partRepository.searchParts(query, pageable)
                .map(partMapper::toDto);
    }

    @Override
    @Transactional
    public PartRevisionDTO createPartRevision(Long partId, PartRevisionDTO revisionDTO) {
        log.info("Creating revision for part id: {}", partId);

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("Part not found with id: " + partId));

        // Check if revision already exists
        partRevisionRepository.findByPartIdAndRevisionAndIteration(
                partId, revisionDTO.getRevision(), revisionDTO.getIteration())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Revision " + revisionDTO.getRevision() +
                            " iteration " + revisionDTO.getIteration() +
                            " already exists for part " + partId);
                });

        // Set previous latest revision to false
        partRevisionRepository.findByPartIdAndIsLatestRevisionTrue(partId)
                .ifPresent(prev -> {
                    prev.setIsLatestRevision(false);
                    partRevisionRepository.save(prev);
                });

        PartRevision revision = partRevisionMapper.toEntity(revisionDTO);
        revision.setPart(part);
        revision.setIsLatestRevision(true);

        PartRevision savedRevision = partRevisionRepository.save(revision);
        log.info("Created revision id: {} for part id: {}", savedRevision.getId(), partId);
        return partRevisionMapper.toDto(savedRevision);
    }

    @Override
    public Page<PartRevisionDTO> listPartRevisions(Long partId, Pageable pageable) {
        log.debug("Listing revisions for part id: {}, pagination: {}", partId, pageable);

        if (!partRepository.existsById(partId)) {
            throw new EntityNotFoundException("Part not found with id: " + partId);
        }

        List<PartRevisionDTO> allRevisions = partRevisionRepository.findByPartIdOrderByRevisionDesc(partId)
                .stream()
                .map(partRevisionMapper::toDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allRevisions.size());

        if (start >= allRevisions.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, allRevisions.size());
        }

        return new PageImpl<>(allRevisions.subList(start, end), pageable, allRevisions.size());
    }
}
