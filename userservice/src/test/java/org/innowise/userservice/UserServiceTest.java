package org.innowise.userservice;

import org.innowise.userservice.dto.UserDTO;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.innowise.userservice.exception.NotFoundException;
import org.innowise.userservice.model.PaymentCard;
import org.innowise.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl service;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setActive(true);
        user.setCards(new ArrayList<>());

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setActive(true);
        userDTO.setCards(new ArrayList<>());
    }

    @Test
    void createUser_shouldSaveUser() {
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = service.createUser(userDTO);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void getUserById_shouldReturnWithoutCards() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = service.getUserById(1L, false);

        assertThat(result).isNotNull();
        verify(userMapper).toDTO(user);
    }

    @Test
    void getUserById_shouldReturnWithCards() {
        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTOWithCards(user)).thenReturn(userDTO);

        UserDTO result = service.getUserById(1L, true);

        assertThat(result).isNotNull();
        verify(userMapper).toDTOWithCards(user);
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(1L, false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserById_shouldThrow_whenInactive() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getUserById(1L, false))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getAllUsers_shouldReturnPage() {
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Page<UserDTO> result = service.getAllUsers(null, null, 0, 10, true);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateUser_shouldUpdateFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = service.updateUser(1L, userDTO);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrow_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(1L, userDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setActive_shouldDeactivateUserAndCards() {
        PaymentCard card = new PaymentCard();
        card.setActive(true);

        user.setCards(List.of(card));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = service.setActive(1L, false);

        assertThat(result).isNotNull();
        assertThat(user.getActive()).isFalse();
        assertThat(card.getActive()).isFalse();
    }
}
