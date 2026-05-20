package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
    @QueryHints(@QueryHint(name = "org.hibernate.timeout", value = "5000"))
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    Optional<Cart> findByUserId(Long userId);
}

