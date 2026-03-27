package org.innowise.userservice.service;

import org.innowise.userservice.dto.PaymentCardDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentCardService {
    /**
     * Create a new card;
     *
     * @param paymentCardDTO data of the card to create;
     * @return returns the result of creating cart.
     */
    PaymentCardDTO createCard(PaymentCardDTO paymentCardDTO);

    /**
     * Get existing card by ID;
     *
     * @param id ID of the card to get;
     * @return returns the card if it exists.
     */
    PaymentCardDTO getPaymentCardById(Long id, Long userId, boolean isUser);

    /**
     * Retrieves a paginated list of payment cards with optional filtering;
     *
     * @param holder optional cardholder name to filter results;
     * @param active optional flag to filter cards by active status;
     * @param page optional flag to filter cards by active status;
     * @param size the number of records per page (must be > 0);
     * @return the cards, that that match the given criteria.
     */
    Page<PaymentCardDTO> getAllCards(String holder, Boolean active, int page, int size);

    /**
     * Get all cards owned by a user;
     *
     * @param id ID of the cardholder;
     * @return returns holder's cards if they exist.
     */
    List<PaymentCardDTO> getPaymentCardsByUserId(Long id);

    /**
     * Update a card's data;
     *
     * @param id ID of the card to update;
     * @param paymentCardDTO new data to be uploaded;
     * @return returns the result of updating the card.
     */
    PaymentCardDTO updatePaymentCard(Long id, PaymentCardDTO paymentCardDTO);

    /**
     * Activate/deactivate a card;
     *
     * @param id ID of a card;
     * @param active new state to be uploaded;
     * @return returns the result of changing the card's state.
     */
    PaymentCardDTO setActive(Long id, boolean active, Long userId, boolean isUser);
}
