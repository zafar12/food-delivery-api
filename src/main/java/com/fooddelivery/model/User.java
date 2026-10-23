package com.fooddelivery.model;

import jakarta.persistence.*;

/**
 * Represents system users including Customers, Restaurant Owners, and Administrators.
 */
@Entity
@Table(name = "users")
public class User {

    public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isImmutableAdmin() {
		return immutableAdmin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setImmutableAdmin(boolean immutableAdmin) {
		this.immutableAdmin = immutableAdmin;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(nullable = false)
    private boolean immutableAdmin = false; // Protects built-in admin from deletion

    public enum Role {
        CUSTOMER,
        RESTAURANT_OWNER,
        ADMINISTRATOR
    }
}