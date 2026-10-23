package com.fooddelivery.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer's order placed at a specific restaurant.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToMany
    @JoinTable(
        name = "order_meals",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "meal_id")
    )
    private List<Meal> meals = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double customTipAmount = 0.0;

    private String couponCode;
    private Double discountApplied = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistories = new ArrayList<>();
    
    
    public User getCustomer() {
		return customer;
	}



	public void setCustomer(User customer) {
		this.customer = customer;
	}



	public Restaurant getRestaurant() {
		return restaurant;
	}



	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}



	public List<Meal> getMeals() {
		return meals;
	}



	public void setMeals(List<Meal> meals) {
		this.meals = meals;
	}



	public LocalDateTime getOrderDate() {
		return orderDate;
	}



	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}



	public Double getTotalAmount() {
		return totalAmount;
	}



	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}



	public Double getCustomTipAmount() {
		return customTipAmount;
	}



	public void setCustomTipAmount(Double customTipAmount) {
		this.customTipAmount = customTipAmount;
	}



	public String getCouponCode() {
		return couponCode;
	}



	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}



	public Double getDiscountApplied() {
		return discountApplied;
	}



	public void setDiscountApplied(Double discountApplied) {
		this.discountApplied = discountApplied;
	}



	public OrderStatus getStatus() {
		return status;
	}



	public void setStatus(OrderStatus status) {
		this.status = status;
	}



	public List<OrderStatusHistory> getStatusHistories() {
		return statusHistories;
	}



	public void setStatusHistories(List<OrderStatusHistory> statusHistories) {
		this.statusHistories = statusHistories;
	}



	public enum OrderStatus {
        PLACED, CANCELED, PROCESSING, IN_ROUTE, DELIVERED, RECEIVED
    }
}