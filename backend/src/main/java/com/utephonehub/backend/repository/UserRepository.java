package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    /**
     * Find users created between start and end date/time
     * Used for user registration chart
     * 
     * @param startDateTime Start date/time
     * @param endDateTime End date/time
     * @return List of users created in the date range
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}

