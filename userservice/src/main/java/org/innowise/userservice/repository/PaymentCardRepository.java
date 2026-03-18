package org.innowise.userservice.repository;

import org.innowise.userservice.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {
    List<PaymentCard> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.active = :active WHERE c.user.id = :userId")
    void setActiveByUserId(@Param("userId") Long userId, @Param("active") boolean active);

    long countByUserId(Long userId);
}
