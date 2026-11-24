package com.example.influencer.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ads")
public class Ads {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "ad_title", nullable = false, length = 255)
	private String adTitle;

	@Column(name = "business_name", nullable = false, length = 255)
	private String businessName;

	@Column(name = "business_email", nullable = false, length = 255)
	private String businessEmail;

	@Column(name = "category", nullable = false, length = 100)
	private String category;

	@Column(name = "budget", nullable = false, precision = 12, scale = 2)
	private BigDecimal budget;

	@Column(name = "ad_type", nullable = false, length = 100)
	private String adType;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAdTitle() {
		return adTitle;
	}

	public void setAdTitle(String adTitle) {
		this.adTitle = adTitle;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getBusinessEmail() {
		return businessEmail;
	}

	public void setBusinessEmail(String businessEmail) {
		this.businessEmail = businessEmail;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BigDecimal getBudget() {
		return budget;
	}

	public void setBudget(BigDecimal budget) {
		this.budget = budget;
	}

	public String getAdType() {
		return adType;
	}

	public void setAdType(String adType) {
		this.adType = adType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
