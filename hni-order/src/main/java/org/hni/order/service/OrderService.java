package org.hni.order.service;

import java.time.LocalDate;
import java.util.Collection;
import org.hni.common.service.BaseService;
import org.hni.order.om.Order;
import org.hni.provider.om.Provider;
import org.hni.user.om.User;

public interface OrderService extends BaseService<Order> {

	/**
	 * Returns a collection of orders for the user
	 * @param user
	 * @return
	 */
	Collection<Order> get(User user);

	/**
	 * Returns a collection of orders for the user created on the date.
	 * @param user
	 * @param startDate
	 * @return
	 */
	Collection<Order> get(User user, LocalDate startDate);
	
	/**
	 * Returns a collection of orders for the user between 2 dates
	 * @param user
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	Collection<Order> get(User user, LocalDate startDate, LocalDate endDate);
		
	/**
	 * Returns the next available order for a provider
	 * Locks an order.  This is to prevent the system from giving the same order to another
	 * @param providerLocation
	 * @return
	 */
	Order next();
	Order next(Provider provider);
	
	/**
	 * Sets an order to completed.
	 * @param order
	 * @return
	 */
	Order complete(Order order);
	
	/**
	 * Forcibly unlock an order.
	 * @param order
	 */
	Order releaseLock(Order order);
	
	/**
	 * Returns a count of open orders
	 * @return
	 */
	long countOrders();
	
	/**
	 * Returns a count of open orders for a provider
	 * @param provider
	 * @return
	 */
	long countOrders(Provider provider);

	Order reset(Order order);
	
	/**
	 * Returns true if the user has reached maximum daily allowed orders
	 * @param user
	 * @return
	 */
	public boolean maxDailyOrdersReached(User user);
	
	/**
	 * Returns true if the user has one or more active authorization codes (not expired or used up)
	 * @param user
	 * @return
	 */
	public boolean hasActiveActivationCodes(User user);
}
