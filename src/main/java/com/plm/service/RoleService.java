package com.plm.service;

import com.plm.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    RoleDTO createRole(RoleDTO roleDTO);

    List<RoleDTO> listRoles();
}
