package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	// Tìm theo orderCode
	Optional<Order> findByOrderCode(String orderCode);

	/**
	 * Calculate total revenue from completed orders
	 * 
	 * @return Total revenue in BigDecimal, returns 0 if no orders
	 */
	@Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
	BigDecimal calculateTotalRevenueByStatus(OrderStatus status);

	/**
	 * Find orders by date range and status
	 * 
	 * @param startDate Start date (inclusive)
	 * @param endDate   End date (inclusive)
	 * @param status    Order status
	 * @return List of orders in date range with specific status
	 */
	List<Order> findByCreatedAtBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);

	/**
	 * Count orders by status
	 * 
	 * @param status Order status
	 * @return Number of orders with specific status
	 */
	long countByStatus(OrderStatus status);

	/**
	 * Find recent orders sorted by created date (newest first) Uses Pageable to
	 * limit results
	 * 
	 * @param pageable Pageable for limiting and sorting
	 * @return List of recent orders
	 */
	List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

	// Tìm tất cả đơn hàng của 1 user
	List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

	// Tìm theo status
	List<Order> findByStatus(OrderStatus status);

	// Tìm đơn hàng theo status và thời gian (cho Cron Job)
	List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time);

	// Kiểm tra orderCode đã tồn tại
	boolean existsByOrderCode(String orderCode);

	// Query fetch cả items (tránh N+1 query)
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
	Optional<Order> findByIdWithItems(@Param("id") Long id);

	// Tìm tất cả đơn hàng của 1 user với User object
	List<Order> findByUserOrderByCreatedAtDesc(User user);

	// Tìm tất cả đơn hàng của 1 user với phân trang
	Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	// Tìm tất cả đơn hàng của 1 user với User object và phân trang
	Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

	// Tìm theo status với User object
	List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);

	// Tìm theo status với userId
	List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

	// Tìm theo orderCode và email
	Optional<Order> findByOrderCodeAndEmail(String orderCode, String email);

	// Admin methods
	Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

	// Đếm số đơn hàng của 1 user với userId
	long countByUserId(Long userId);

	// Đếm số đơn hàng của 1 user với User object
	@Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user")
	long countByUser(@Param("user") User user);

	// ========================================
	// ✅ THÊM METHODS CHO PUBLIC TRACKING
	// ========================================

	// Validate orderCode và email tồn tại
	boolean existsByOrderCodeAndEmail(String orderCode, String email);

	// Tìm đơn hàng gần đây từ ngày cụ thể
	@Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate ORDER BY o.createdAt DESC")
	List<Order> findRecentOrders(@Param("startDate") LocalDateTime startDate);

	// Tìm đơn hàng theo khoảng thời gian và trạng thái
	@Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = :status")
	List<Order> findByDateRangeAndStatus(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate, @Param("status") OrderStatus status);

	// ========================================
	// ✅ NEW ADMIN METHODS
	// ========================================

	// Tìm đơn hàng với nhiều bộ lọc và phân trang
	// Thay thế findOrdersWithFlexibleFilters bằng version đơn giản:

	@Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " + "(:search IS NULL OR :search = '' OR "
			+ " LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ " LOWER(o.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ " LOWER(o.recipientName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ " LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ " (u IS NOT NULL AND LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')))" + ") AND " +
			// SỬ DỤNG SpEL CHO CÁC THAM SỐ CÓ KIỂU DỮ LIỆU ĐẶC BIỆT
			"(:#{#status == null} = true OR o.status = :status) AND "
			+ "(:#{#paymentMethod == null} = true OR o.paymentMethod = :paymentMethod) AND "
			+ "(:#{#customerId == null} = true OR (u IS NOT NULL AND u.id = :customerId)) AND "
			+ "(:customerEmail IS NULL OR :customerEmail = '' OR "
			+ " LOWER(o.email) LIKE LOWER(CONCAT('%', :customerEmail, '%'))" + ") AND "
			+ "(:#{#fromDate == null} = true OR o.createdAt >= :fromDate) AND "
			+ "(:#{#toDate == null} = true OR o.createdAt <= :toDate) AND "
			+ "(:#{#minAmount == null} = true OR o.totalAmount >= :minAmount) AND "
			+ "(:#{#maxAmount == null} = true OR o.totalAmount <= :maxAmount)")
	Page<Order> findOrdersWithFlexibleFilters(@Param("search") String search, @Param("status") OrderStatus status,
			@Param("paymentMethod") PaymentMethod paymentMethod, @Param("customerId") Long customerId,
			@Param("customerEmail") String customerEmail, @Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate, @Param("minAmount") BigDecimal minAmount,
			@Param("maxAmount") BigDecimal maxAmount, Pageable pageable);

	// Đem số đơn hàng theo từng trạng thái
	@Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
	List<Object[]> countOrdersByStatus();

	// Tìm đơn hàng gần đây với giới hạn số lượng
	@Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
	List<Order> findRecentOrdersLimit(Pageable pageable);

	// Tìm tất cả đơn hàng của 1 user với User object, kèm theo chi tiết (items)
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items WHERE o.user.id = :userId")
	List<Order> findOrdersByUserWithDetails(@Param("userId") Long userId);

	// Thống kê tổng doanh thu trong khoảng thời gian
	@Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :startDate AND :endDate")
	BigDecimal getTotalRevenueInDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	// Tìm top khách hàng theo số đơn hàng đã đặt
	@Query("SELECT o.user.id, o.user.fullName, o.user.email, COUNT(o) as orderCount, SUM(o.totalAmount) as totalSpent "
			+ "FROM Order o WHERE o.user IS NOT NULL " + "GROUP BY o.user.id, o.user.fullName, o. user.email "
			+ "ORDER BY COUNT(o) DESC")
	List<Object[]> findTopCustomersByOrderCount(Pageable pageable);

}
