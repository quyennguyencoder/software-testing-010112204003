package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    
    /**
     * Tính trung bình rating của 1 sản phẩm
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(Long productId);
    
    /**
     * Đếm số lượng review của 1 sản phẩm
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(Long productId);
    
    /**
     * Batch query: Lấy rating và review count cho nhiều sản phẩm
     * Returns: [productId, avgRating, reviewCount]
     */
    @Query("SELECT r.product.id as productId, " +
           "AVG(r.rating) as avgRating, " +
           "COUNT(r) as reviewCount " +
           "FROM Review r " +
           "WHERE r.product.id IN :productIds " +
           "GROUP BY r.product.id")
    List<Object[]> getReviewStatsByProductIds(List<Long> productIds);
    
    /**
     * Lấy danh sách product IDs có rating >= minRating
     * Dùng cho Featured Products - KHÔNG BỎ SÓT
     * Sắp xếp theo avgRating DESC, reviewCount DESC
     */
    @Query("SELECT r.product.id, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
           "FROM Review r " +
           "WHERE r.product.status = true AND r.product.isDeleted = false " +
           "GROUP BY r.product.id " +
           "HAVING AVG(r.rating) >= :minRating " +
           "ORDER BY avgRating DESC, reviewCount DESC")
    List<Object[]> findProductIdsWithHighRating(Double minRating);
    
    /**
     * Lấy TOP sản phẩm có rating cao nhất (không cần threshold)
     * Returns: [productId, avgRating, reviewCount]
     */
    @Query("SELECT r.product.id, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
           "FROM Review r " +
           "WHERE r.product.status = true AND r.product.isDeleted = false " +
           "GROUP BY r.product.id " +
           "ORDER BY avgRating DESC, reviewCount DESC")
    List<Object[]> findTopRatedProductIds();
}

