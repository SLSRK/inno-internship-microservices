package org.innowise.userservice.specification;

import org.innowise.userservice.model.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {
    public static Specification<PaymentCard> hasHolder(String holder) {

        return (root, query, cb) -> {

            if (holder == null || holder.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("holder")),
                    "%" + holder.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PaymentCard> isActive(Boolean active) {

        return (root, query, cb) -> {

            if (active == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("active"), active);
        };
    }
}
