package org.hni.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import javax.inject.Inject;

import org.hni.common.service.AbstractService;
import org.hni.order.dao.OrderDAO;
import org.hni.order.om.Order;
import org.hni.order.service.LockingService;
import org.hni.payment.dao.OrderPaymentDAO;
import org.hni.payment.dao.PaymentInstrumentDAO;
import org.hni.payment.om.OrderPayment;
import org.hni.payment.om.PaymentInfo;
import org.hni.payment.om.PaymentInstrument;
import org.hni.provider.om.Provider;
import org.hni.user.om.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DefaultOrderPaymentService extends AbstractService<OrderPayment> implements OrderPaymentService {
	private static final Logger logger = LoggerFactory.getLogger(DefaultOrderPaymentService.class);
	private static final Long DEFAULT_CARD_LOCKOUT_MINS = 5L;
	
	private OrderPaymentDAO orderPaymentDao;
	private PaymentInstrumentDAO paymentInstrumentDao;
	private LockingService lockingService;
	private OrderDAO orderDao;
	
	@Inject
	public DefaultOrderPaymentService(OrderPaymentDAO orderPaymentDao, PaymentInstrumentDAO paymentInstrumentDao, OrderDAO orderDao, LockingService lockingService) {
		super(orderPaymentDao);
		this.orderPaymentDao = orderPaymentDao;
		this.paymentInstrumentDao = paymentInstrumentDao;
		this.orderDao = orderDao;
		this.lockingService = lockingService;
	}

	@Override
	public Order assignPayment(Collection<PaymentInfo> paymentInfos, User user) {
		Order order = null;
		for(PaymentInfo pi : paymentInfos) {
			logger.info(String.format("Recording payment %d - %d - %.2f", pi.getOrderId(), pi.getPaymentInstrumentId(), pi.getAmountUsed()));
			if (null == order) {
				order = orderDao.get(pi.getOrderId());
			}
			PaymentInstrument paymentInstrument = paymentInstrumentDao.get(pi.getPaymentInstrumentId());			
			OrderPayment orderPayment = new OrderPayment(order, paymentInstrument, pi.getAmountUsed(), user);
			
			BigDecimal cardTotal = BigDecimal.valueOf(paymentInstrument.getBalance()).subtract(BigDecimal.valueOf(pi.getAmountUsed()));
			paymentInstrument.setBalance(cardTotal.doubleValue());
			paymentInstrumentDao.save(paymentInstrument);

			this.save(orderPayment);
		}
		return order;
	}
		
	/**
	 * Return CARD, amount to use on this card
	 * @throws PaymentsExceededException 
	 */
	@Override
	public Collection<OrderPayment> paymentFor(Order order, Provider provider, Double amount, User user) throws PaymentsExceededException {
		Collection<PaymentInstrument> providerCards = paymentInstrumentDao.with(provider);
		Collection<OrderPayment> orderPayments = new HashSet();
		
		BigDecimal theAmount = BigDecimal.valueOf(amount);
		//BigDecimal totalAmount = calcExistingPayments(order);
		
		for(PaymentInstrument paymentInstrument : providerCards) {
			if ( lockingService.acquireLock(lockingKey(paymentInstrument), DEFAULT_CARD_LOCKOUT_MINS)  ) {
				logger.info("locking card "+lockingKey(paymentInstrument));
				BigDecimal amountToDispense = calcAmountToDispense(paymentInstrument, theAmount);
				theAmount = theAmount.subtract(amountToDispense);
				//totalAmount = totalAmount.add(amountToDispense);
				
				OrderPayment orderPayment = new OrderPayment(order, paymentInstrument, amountToDispense.doubleValue(), user);
				orderPayments.add(orderPayment);

				paymentInstrument.setLastUsedDatetime(new Date());
				paymentInstrumentDao.save(paymentInstrument);
				
				if ( theAmount.doubleValue() <= 0 ) {
					break;
				}
			}
		}
		
		if (totalAmountRequestedExceedsTotal(order, orderPayments)) {
			throw new PaymentsExceededException("You have requested more funds than you really need.  Shutting down this account...");
		}		
		return orderPayments;
	}
	
	@Override
	public Collection<OrderPayment> paymentsFor(Order order) { 
		return orderPaymentDao.paymentsFor(order);
	}

	private BigDecimal calcAmountToDispense(PaymentInstrument paymentInstrument, BigDecimal amount) {
		if (paymentInstrument.getBalance().doubleValue() >= amount.doubleValue()) {
			return amount;
		}
		// the amount on the card is less than we need, so return the full amount on the card.
		return BigDecimal.valueOf(paymentInstrument.getBalance());		
	}
	
	private String lockingKey(PaymentInstrument paymentInstrument) {
		return String.format("paymentInstrument:%d", paymentInstrument.getId());
	}
	
	private Double addOrderAmount(Order order) {
		double total = order.getOrderItems().stream().mapToDouble(o -> o.getMenuItem().getPrice()).sum();
		return total;
	}
	
	private boolean totalAmountRequestedExceedsTotal(Order order, Collection<OrderPayment> orderPayments) {
		Double total = 1.25*addOrderAmount(order); // add 25%
		Collection<OrderPayment> prevOrderPayments = (Collection<OrderPayment>) lockingService.getCache(orderPaymentsKey(order));
		Collection<OrderPayment> allPayments = new ArrayList<OrderPayment>(orderPayments);
		allPayments.addAll(prevOrderPayments);
		lockingService.addCache(orderPaymentsKey(order), allPayments);
		
		double paymentTotal = allPayments.stream().mapToDouble(o -> o.getAmount()).sum(); 
		return (paymentTotal > total);
	}

	private String orderPaymentsKey(Order order) {
		return String.format("order-payments:%d", order.getId());
	}
}
