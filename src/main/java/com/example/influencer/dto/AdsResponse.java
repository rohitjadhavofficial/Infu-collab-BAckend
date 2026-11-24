package com.example.influencer.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.influencer.entity.Ads;

public class AdsResponse {

	private Long id;
	private String adTitle;
	private String businessName;
	private String businessEmail;
	private String category;
	private BigDecimal budget;
	private String adType;
	private String description;
	private Instant createdAt;

	public AdsResponse() {
	}

	public static AdsResponse from(Ads ads) {
		AdsResponse response = new AdsResponse();
		response.setId(ads.getId());
		response.setAdTitle(ads.getAdTitle());
		response.setBusinessName(ads.getBusinessName());
		response.setBusinessEmail(ads.getBusinessEmail());
		response.setCategory(ads.getCategory());
		response.setBudget(ads.getBudget());
		response.setAdType(ads.getAdType());
		response.setDescription(ads.getDescription());
		response.setCreatedAt(ads.getCreatedAt());
		return response;
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
