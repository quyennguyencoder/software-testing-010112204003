
package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.order.TrackOrderRequest;
import com.utephonehub.backend.dto.response.order.PublicOrderTrackingResponse;

public interface IPublicOrderService {

	/**
	 * Track order by order code and email (public access)
	 * 
	 * @param request Order code and email
	 * @return Public order tracking information
	 */
	PublicOrderTrackingResponse trackOrder(TrackOrderRequest request);

	/**
	 * Quick track by order code only (limited info)
	 * 
	 * @param orderCode Order code
	 * @return Basic order status information
	 */
	PublicOrderTrackingResponse quickTrackByCode(String orderCode);

	/**
	 * Validate order code and email combination
	 * 
	 * @param orderCode Order code
	 * @param email     Customer email
	 * @return True if valid combination
	 */
	boolean validateOrderAccess(String orderCode, String email);

	/**
	 * Get order tracking statistics (for admin dashboard)
	 * 
	 * @return Tracking statistics
	 */
	Object getTrackingStatistics();
}