package com.fooddelivery.service;

import com.fooddelivery.entity.Coupon;
import com.fooddelivery.repository.CouponRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon validateAndGetCoupon(String code, Double orderSubtotal) {
        if (code == null || code.trim().isEmpty()) {
            return null; // No coupon provided
        }

        Coupon coupon = couponRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new EntityNotFoundException("Invalid coupon code: " + code));

        if (!coupon.isActive()) {
            throw new IllegalStateException("This coupon is inactive.");
        }

        if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("This coupon has expired.");
        }

        if (coupon.getMinOrderValue() != null && orderSubtotal < coupon.getMinOrderValue()) {
            throw new IllegalArgumentException("Order subtotal must be at least " + coupon.getMinOrderValue() + " to use this coupon.");
        }

        return coupon;
    }

    public Double calculateDiscount(Double subtotal, Coupon coupon) {
        if (coupon == null) {
            return 0.0;
        }
        // Calculate percentage discount (e.g., 15% -> subtotal * 0.15)
        return subtotal * (coupon.getDiscountPercentage() / 100.0);
    }
}