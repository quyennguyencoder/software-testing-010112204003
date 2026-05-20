package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find top selling products based on total quantity sold
     * Only count orders with DELIVERED status (completed orders)
     * 
     * @param pageable Pageable limiting the number of products to return
     * @return List of Object arrays containing: [Product, totalQuantity, totalRevenue]
     */
    @Query("SELECT oi.product, " +
           "SUM(oi.quantity) as totalQuantity, " +
           "SUM(oi.quantity * oi.price) as totalRevenue " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY oi.product " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
    
    /**
     * Đếm tổng số lượng đã bán của 1 sản phẩm (chỉ đơn hàng DELIVERED)
     * 
     * @param productId ID của sản phẩm
     * @return Tổng số lượng đã bán
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.product.id = :productId " +
           "AND o.status = 'DELIVERED'")
    Integer countSoldQuantityByProductId(Long productId);
    
    /**
     * Batch query: Đếm số lượng đã bán cho nhiều sản phẩm cùng lúc
     * Tránh N+1 query problem
     * 
     * @param productIds Danh sách ID sản phẩm
     * @return Map<ProductId, SoldCount>
     */
    @Query("SELECT oi.product.id as productId, COALESCE(SUM(oi.quantity), 0) as soldCount " +
           "FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.product.id IN :productIds " +
           "AND o.status = 'DELIVERED' " +
           "GROUP BY oi.product.id")
    List<Object[]> countSoldQuantityByProductIds(List<Long> productIds);
    
    // Tìm tất cả items của 1 order
    List<OrderItem> findByOrderId(Long orderId);
}
