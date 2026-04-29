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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private Role role;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .isActive(true)
                .roles(new HashSet<>())
                .build();

        role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator")
                .build();

        roleDTO = RoleDTO.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator")
                .build();
    }

    // ==================== createUser ====================

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void createUser_success() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userMapper.toEntity(any(UserDTO.class))).thenReturn(user);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toDto(any(User.class))).thenReturn(userDTO);

            UserDTO result = userService.createUser(userDTO, "password123");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
            assertThat(user.getIsActive()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw when username already exists")
        void createUser_duplicateUsername() {
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(userDTO, "password"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should throw when email already exists")
        void createUser_duplicateEmail() {
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(userDTO, "password"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email")
                    .hasMessageContaining("already exists");
        }
    }

    // ==================== getUserById ====================

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void getUserById_found() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toDto(user)).thenReturn(userDTO);

            UserDTO result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when user not found")
        void getUserById_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ==================== getUserByUsername ====================

    @Nested
    @DisplayName("getUserByUsername")
    class GetUserByUsername {

        @Test
        @DisplayName("should return user when found by username")
        void getUserByUsername_found() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(userMapper.toDto(user)).thenReturn(userDTO);

            UserDTO result = userService.getUserByUsername("testuser");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw when username not found")
        void getUserByUsername_notFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== updateUser ====================

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void updateUser_success() {
            UserDTO updateDTO = UserDTO.builder()
                    .username("testuser")
                    .email("updated@example.com")
                    .fullName("Updated Name")
                    .isActive(true)
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toDto(any(User.class))).thenReturn(updateDTO);

            UserDTO result = userService.updateUser(1L, updateDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("updated@example.com");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw when user not found")
        void updateUser_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99L, userDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when changing to duplicate username")
        void updateUser_duplicateUsername() {
            UserDTO updateDTO = UserDTO.builder()
                    .username("newuser")
                    .email("test@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should throw when changing to duplicate email")
        void updateUser_duplicateEmail() {
            UserDTO updateDTO = UserDTO.builder()
                    .username("testuser")
                    .email("new@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email")
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should throw OptimisticLockException on save conflict")
        void updateUser_optimisticLock() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenThrow(new OptimisticLockException("conflict"));

            assertThatThrownBy(() -> userService.updateUser(1L, userDTO))
                    .isInstanceOf(OptimisticLockException.class);
        }
    }

    // ==================== deleteUser ====================

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void deleteUser_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doNothing().when(userRepository).delete(user);

            userService.deleteUser(1L);

            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("should throw when user not found")
        void deleteUser_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw OptimisticLockException on delete conflict")
        void deleteUser_optimisticLock() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doThrow(new OptimisticLockException("conflict")).when(userRepository).delete(user);

            assertThatThrownBy(() -> userService.deleteUser(1L))
                    .isInstanceOf(OptimisticLockException.class);
        }
    }

    // ==================== listUsers ====================

    @Nested
    @DisplayName("listUsers")
    class ListUsers {

        @Test
        @DisplayName("should return paginated users")
        void listUsers_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.toDto(user)).thenReturn(userDTO);

            Page<UserDTO> result = userService.listUsers(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==================== assignRole ====================

    @Nested
    @DisplayName("assignRole")
    class AssignRole {

        @Test
        @DisplayName("should assign role to user")
        void assignRole_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(roleMapper.toDto(role)).thenReturn(roleDTO);

            Set<RoleDTO> result = userService.assignRole(1L, 1L);

            assertThat(result).hasSize(1);
            assertThat(result.iterator().next().getName()).isEqualTo("ADMIN");
            assertThat(user.getRoles()).contains(role);
        }

        @Test
        @DisplayName("should throw when user not found")
        void assignRole_userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignRole(99L, 1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when role not found")
        void assignRole_roleNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.assignRole(1L, 99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Role not found");
        }
    }

    // ==================== getUserRoles ====================

    @Nested
    @DisplayName("getUserRoles")
    class GetUserRoles {

        @Test
        @DisplayName("should return user roles")
        void getUserRoles_success() {
            user.getRoles().add(role);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleMapper.toDto(role)).thenReturn(roleDTO);

            Set<RoleDTO> result = userService.getUserRoles(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty set when user has no roles")
        void getUserRoles_empty() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            Set<RoleDTO> result = userService.getUserRoles(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when user not found")
        void getUserRoles_userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserRoles(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ==================== removeRole ====================

    @Nested
    @DisplayName("removeRole")
    class RemoveRole {

        @Test
        @DisplayName("should remove role from user")
        void removeRole_success() {
            user.getRoles().add(role);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.removeRole(1L, 1L);

            assertThat(user.getRoles()).doesNotContain(role);
        }

        @Test
        @DisplayName("should throw when user not found")
        void removeRole_userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.removeRole(99L, 1L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when role not found")
        void removeRole_roleNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.removeRole(1L, 99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when user does not have the role")
        void removeRole_userDoesNotHaveRole() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

            assertThatThrownBy(() -> userService.removeRole(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not have role");
        }
    }
}
