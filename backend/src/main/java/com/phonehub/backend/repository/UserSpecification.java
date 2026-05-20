package com.phonehub.backend.repository;

import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.UserRole;
import com.phonehub.backend.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filterUsers(UserRole role, UserStatus status, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by role (if not ALL)
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            // Filter by status (if not ALL)
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Search by username, email, or fullName
            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate usernamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")), likePattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), likePattern);
                Predicate fullNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")), likePattern);

                predicates.add(criteriaBuilder.or(usernamePredicate, emailPredicate, fullNamePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
