package com.plm.service;

import com.plm.config.CacheConfig;
import com.plm.dto.AssemblyDTO;
import com.plm.entity.Assembly;
import com.plm.mapper.AssemblyMapper;
import com.plm.repository.AssemblyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssemblyServiceImpl implements AssemblyService {

    private final AssemblyRepository assemblyRepository;
    private final AssemblyMapper assemblyMapper;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.ASSEMBLIES_CACHE, allEntries = true)
    public AssemblyDTO createAssembly(AssemblyDTO assemblyDTO) {
        log.info("Creating assembly with number: {}", assemblyDTO.getPartNumber());

        if (assemblyRepository.existsByPartNumber(assemblyDTO.getPartNumber())) {
            throw new IllegalArgumentException("Assembly with number " + assemblyDTO.getPartNumber() + " already exists");
        }

        Assembly assembly = assemblyMapper.toEntity(assemblyDTO);
        Assembly savedAssembly = assemblyRepository.save(assembly);
        log.info("Created assembly with id: {}", savedAssembly.getId());
        return assemblyMapper.toDTO(savedAssembly);
    }

    @Override
    @Cacheable(value = CacheConfig.ASSEMBLIES_CACHE, key = "#id")
    public AssemblyDTO getAssemblyById(Long id) {
        log.debug("Fetching assembly by id: {}", id);
        Assembly assembly = assemblyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assembly not found with id: " + id));
        return assemblyMapper.toDTO(assembly);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.ASSEMBLIES_CACHE, allEntries = true)
    public AssemblyDTO updateAssembly(Long id, AssemblyDTO assemblyDTO) {
        log.info("Updating assembly with id: {}", id);

        Assembly existingAssembly = assemblyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assembly not found with id: " + id));

        // Check if partNumber is being changed and if new number already exists
        if (!existingAssembly.getPartNumber().equals(assemblyDTO.getPartNumber()) &&
            assemblyRepository.existsByPartNumber(assemblyDTO.getPartNumber())) {
            throw new IllegalArgumentException("Assembly with number " + assemblyDTO.getPartNumber() + " already exists");
        }

        existingAssembly.setPartNumber(assemblyDTO.getPartNumber());
        existingAssembly.setName(assemblyDTO.getName());
        existingAssembly.setDescription(assemblyDTO.getDescription());
        existingAssembly.setRevision(assemblyDTO.getRevision());
        existingAssembly.setLifecycleState(assemblyDTO.getLifecycleState());

        Assembly updatedAssembly = assemblyRepository.save(existingAssembly);
        log.info("Updated assembly with id: {}", updatedAssembly.getId());
        return assemblyMapper.toDTO(updatedAssembly);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.ASSEMBLIES_CACHE, allEntries = true)
    public void deleteAssembly(Long id) {
        log.info("Deleting assembly with id: {}", id);

        Assembly assembly = assemblyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assembly not found with id: " + id));

        assemblyRepository.delete(assembly);
        log.info("Deleted assembly with id: {}", id);
    }

    @Override
    public Page<AssemblyDTO> listAssemblies(Pageable pageable) {
        log.debug("Listing assemblies with pagination: {}", pageable);
        return assemblyRepository.findAll(pageable)
                .map(assemblyMapper::toDTO);
    }
}
