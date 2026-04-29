package com.plm.service;

import com.plm.dto.RoleDTO;
import com.plm.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UserService {

    UserDTO createUser(UserDTO userDTO, String password);

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    Page<UserDTO> listUsers(Pageable pageable);

    Set<RoleDTO> assignRole(Long userId, Long roleId);

    Set<RoleDTO> getUserRoles(Long userId);

    void removeRole(Long userId, Long roleId);
}
