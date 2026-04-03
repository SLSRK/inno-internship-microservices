package org.innowise.orderservice.specification;

import org.innowise.orderservice.model.Order;
import org.innowise.orderservice.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecification {
    public static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Order> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Order> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Order> notDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }
}
