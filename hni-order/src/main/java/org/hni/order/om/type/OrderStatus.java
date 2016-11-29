package org.hni.order.om.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderStatus {

	public static OrderStatus OPEN = new OrderStatus(1L, "Open");
	public static OrderStatus ORDERED = new OrderStatus(2L, "Order Placed");
	public static OrderStatus CLOSED = new OrderStatus(3L, "Closed");
	
	private Long id;
	private String name;
	
	private OrderStatus(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Long getId() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	
	public static OrderStatus get(Long id) {
		for (OrderStatus type : TYPES) {
			if (type.getId().equals(id)) {
				return type;
			}
		}
		return null;
	}

	private static final OrderStatus[] TYPES = { OPEN, ORDERED, CLOSED};
	public static final List<OrderStatus> VALUES = Collections.unmodifiableList(Arrays.asList(TYPES));


}
