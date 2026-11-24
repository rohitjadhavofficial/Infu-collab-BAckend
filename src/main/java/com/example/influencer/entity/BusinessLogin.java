package com.example.influencer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "business_login", uniqueConstraints = @UniqueConstraint(name = "uk_business_login_email", columnNames = "email"))
public class BusinessLogin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "business_name", nullable = false, length = 255)
	private String businessName;

	@Column(name = "category", nullable = false, length = 100)
	private String category;

	@Column(name = "city", nullable = false, length = 100)
	private String city;

	@Column(name = "email", nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Column(name = "password", nullable = false, length = 255)
	private String password;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
