package org.innowise.userservice;

import org.innowise.userservice.dto.PaymentCardDTO;
import org.innowise.userservice.exception.CardsQuantityException;
import org.innowise.userservice.exception.NotActiveException;
import org.innowise.userservice.exception.NotFoundException;
import org.innowise.userservice.mapper.PaymentCardMapper;
import org.innowise.userservice.model.PaymentCard;
import org.innowise.userservice.model.User;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.service.impl.PaymentCardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
public class PaymentCardServiceTest {
    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private PaymentCardServiceImpl service;

    private User user;
    private PaymentCard card;
    private PaymentCardDTO cardDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setActive(true);
        user.setCards(new ArrayList<>());

        card = new PaymentCard();
        card.setId(1L);
        card.setActive(true);
        card.setUser(user);

        cardDTO = new PaymentCardDTO();
        cardDTO.setUserId(1L);
    }

    @Test
    void createCard_shouldSaveCard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardMapper.toEntity(cardDTO)).thenReturn(card);
        when(paymentCardRepository.save(any())).thenReturn(card);
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);
        when(cacheManager.getCache("users")).thenReturn(cache);

        PaymentCardDTO result = service.createCard(cardDTO);

        assertThat(result).isNotNull();
        verify(paymentCardRepository).save(any());
        verify(cache).evict(1L);
    }

    @Test
    void createCard_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCard(cardDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createCard_shouldThrow_whenUserInactive() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.createCard(cardDTO))
                .isInstanceOf(NotActiveException.class);
    }

    @Test
    void createCard_shouldThrow_whenLimitExceeded() {
        user.setCards(List.of(new PaymentCard(), new PaymentCard(),
                new PaymentCard(), new PaymentCard(), new PaymentCard()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.createCard(cardDTO))
                .isInstanceOf(CardsQuantityException.class);
    }

    @Test
    void getPaymentCardById_shouldReturnCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);

        PaymentCardDTO result = service.getPaymentCardById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getPaymentCardById_shouldThrow_whenInactive() {
        card.setActive(false);
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> service.getPaymentCardById(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getAllCards_shouldReturnPage() {
        Page<PaymentCard> page = new PageImpl<>(List.of(card));

        when(paymentCardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);

        Page<PaymentCardDTO> result = service.getAllCards(null, true, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getPaymentCardsByUserId_shouldReturnActiveCards() {
        when(userRepository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.findByUserId(1L)).thenReturn(List.of(card));
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);

        List<PaymentCardDTO> result = service.getPaymentCardsByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(cardDTO);
    }

    @Test
    void updatePaymentCard_shouldUpdateFields() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(any())).thenReturn(card);
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);

        PaymentCardDTO result = service.updatePaymentCard(1L, cardDTO);

        assertThat(result).isNotNull();
        verify(paymentCardRepository).save(card);
    }

    @Test
    void setActive_shouldUpdateStatus() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(any())).thenReturn(card);
        when(paymentCardMapper.toDTO(card)).thenReturn(cardDTO);

        PaymentCardDTO result = service.setActive(1L, false);

        assertThat(result).isNotNull();
        assertThat(card.getActive()).isFalse();
    }
}
