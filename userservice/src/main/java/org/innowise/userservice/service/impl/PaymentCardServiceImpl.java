package org.innowise.userservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.innowise.userservice.dto.PaymentCardDTO;
import org.innowise.userservice.exception.AccessDeniedException;
import org.innowise.userservice.exception.CardsQuantityException;
import org.innowise.userservice.exception.NotActiveException;
import org.innowise.userservice.exception.NotFoundException;
import org.innowise.userservice.mapper.PaymentCardMapper;
import org.innowise.userservice.model.PaymentCard;
import org.innowise.userservice.model.User;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.service.PaymentCardService;
import org.innowise.userservice.specification.PaymentCardSpecification;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;
    private final CacheManager cacheManager;

    private final String notFound = "Card not found";
    private final String notActive = "Card not active";

    @Transactional
    public PaymentCardDTO createCard(PaymentCardDTO paymentCardDTO){
        User user = userRepository.findById(paymentCardDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (paymentCardRepository.countByUserId(user.getId()) >= 5) {
            throw new CardsQuantityException("This user already has 5 cards");
        }
        if(!user.getActive()){
            throw new NotActiveException("User not active");
        }
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDTO);
        paymentCard.setId(null);
        paymentCard.setUser(user);
        PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);

        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").evict(user.getId());
        }

        return paymentCardMapper.toDTO(savedPaymentCard);
    }

    public PaymentCardDTO getPaymentCardById(Long id, Long userId, boolean isUser){
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(notFound));

        if(paymentCard.getUser().getId() != userId && isUser){
            throw new AccessDeniedException("Access denied");
        }

        if(!paymentCard.getActive()) {
            throw new RuntimeException(notActive);
        }

        return paymentCardMapper.toDTO(paymentCard);
    }

    public Page<PaymentCardDTO> getAllCards(
            String holder,
            Boolean active,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasHolder(holder))
                .and(PaymentCardSpecification.isActive(active));

        return paymentCardRepository.findAll(spec, pageable)
                .map(paymentCardMapper::toDTO);
    }

    public List<PaymentCardDTO> getPaymentCardsByUserId(Long id){
        User user = userRepository.findByIdWithCards(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return user.getCards().stream()
                .filter(PaymentCard::getActive)
                .map(paymentCardMapper::toDTO)
                .toList();
    }

    @Transactional
    @CachePut(value = "cards", key = "#id")
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result.userId + '_true'"),
            @CacheEvict(value = "users", key = "#result.userId + '_false'")
    })
    public PaymentCardDTO updatePaymentCard(Long id, PaymentCardDTO paymentCardDTO){
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(notFound));

        paymentCard.setNumber(paymentCardDTO.getNumber());
        paymentCard.setHolder(paymentCardDTO.getHolder());
        paymentCard.setExpirationDate(paymentCardDTO.getExpirationDate());
        return paymentCardMapper.toDTO(paymentCardRepository.save(paymentCard));
    }

    @Transactional
    @CachePut(value = "cards", key = "#id")
    public PaymentCardDTO setActive(Long id, boolean active, Long userId, boolean isUser){
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(notFound));

        if(paymentCard.getUser().getId() != userId && isUser){
            throw new AccessDeniedException("Access denied");
        }

        paymentCard.setActive(active);
        return paymentCardMapper.toDTO(paymentCardRepository.save(paymentCard));
    }
}
