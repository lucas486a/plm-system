package com.plm.service;

import com.plm.dto.RoleDTO;
import com.plm.dto.UserDTO;
import com.plm.entity.Role;
import com.plm.entity.User;
import com.plm.mapper.RoleMapper;
import com.plm.mapper.UserMapper;
import com.plm.repository.RoleRepository;
import com.plm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO, String password) {
        log.info("Creating user with username: {}", userDTO.getUsername());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("User with username " + userDTO.getUsername() + " already exists");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("User with email " + userDTO.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(userDTO);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Created user with id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Check if username is being changed and if new username already exists
        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("User with username " + userDTO.getUsername() + " already exists");
        }

        // Check if email is being changed and if new email already exists
        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("User with email " + userDTO.getEmail() + " already exists");
        }

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setFullName(userDTO.getFullName());
        existingUser.setIsActive(userDTO.getIsActive());

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("Updated user with id: {}", updatedUser.getId());
            return userMapper.toDto(updatedUser);
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock conflict for user id: {}", id);
            throw new OptimisticLockException("User was modified by another user. Please refresh and try again.");
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        try {
            userRepository.delete(user);
            log.info("Deleted user with id: {}", id);
        } catch (OptimisticLockException e) {
            log.error("Optimistic lock conflict for user id: {}", id);
            throw new OptimisticLockException("User was modified by another user. Please refresh and try again.");
        }
    }

    @Override
    public Page<UserDTO> listUsers(Pageable pageable) {
        log.debug("Listing users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public Set<RoleDTO> assignRole(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        user.getRoles().add(role);
        userRepository.save(user);

        log.info("Assigned role {} to user {}", roleId, userId);
        return user.getRoles().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<RoleDTO> getUserRoles(Long userId) {
        log.debug("Fetching roles for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return user.getRoles().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void removeRole(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (!user.getRoles().contains(role)) {
            throw new IllegalArgumentException("User does not have role with id: " + roleId);
        }

        user.getRoles().remove(role);
        userRepository.save(user);

        log.info("Removed role {} from user {}", roleId, userId);
    }
}
