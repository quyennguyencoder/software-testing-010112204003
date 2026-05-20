package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE) // Prevent direct setter, use helper methods instead
    private List<CartItem> items = new ArrayList<>();

    /**
     * Get unmodifiable view of cart items
     * @return unmodifiable list of cart items
     */
    public List<CartItem> getItems() {
        return items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
    }

    /**
     * Get mutable items list for JPA and internal use only
     * @return mutable items list
     */
    public List<CartItem> getItemsInternal() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    /**
     * Add item to cart safely
     * @param item cart item to add
     */
    public void addItem(CartItem item) {
        getItemsInternal().add(item);
        item.setCart(this);
    }

    /**
     * Remove item from cart safely
     * @param item cart item to remove
     */
    public void removeItem(CartItem item) {
        getItemsInternal().remove(item);
        item.setCart(null);
    }

    /**
     * Clear all items from cart
     */
    public void clearItems() {
        getItemsInternal().forEach(item -> item.setCart(null));
        getItemsInternal().clear();
    }
}

