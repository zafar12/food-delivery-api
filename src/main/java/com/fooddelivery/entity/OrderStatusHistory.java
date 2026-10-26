package com.fooddelivery.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks the history of status transitions for an order.
 */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime changedAt;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Order.OrderStatus getStatus() {
		return status;
	}

	public void setStatus(Order.OrderStatus status) {
		this.status = status;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}
    
    
}