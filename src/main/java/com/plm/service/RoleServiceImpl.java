package com.plm.service;

import com.plm.dto.RoleDTO;
import com.plm.entity.Role;
import com.plm.mapper.RoleMapper;
import com.plm.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        log.info("Creating role with name: {}", roleDTO.getName());

        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name " + roleDTO.getName() + " already exists");
        }

        Role role = roleMapper.toEntity(roleDTO);
        Role savedRole = roleRepository.save(role);
        log.info("Created role with id: {}", savedRole.getId());
        return roleMapper.toDto(savedRole);
    }

    @Override
    public List<RoleDTO> listRoles() {
        log.debug("Listing all roles");
        return roleMapper.toDtoList(roleRepository.findAll());
    }
}
