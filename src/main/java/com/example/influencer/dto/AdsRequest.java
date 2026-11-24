package com.example.influencer.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdsRequest {

	@NotBlank(message = "adTitle is required")
	@Size(max = 255, message = "adTitle must not exceed 255 characters")
	private String adTitle;

	@NotBlank(message = "businessName is required")
	@Size(max = 255, message = "businessName must not exceed 255 characters")
	private String businessName;

	@NotBlank(message = "businessEmail is required")
	@Email(message = "businessEmail must be a valid email address")
	@Size(max = 255, message = "businessEmail must not exceed 255 characters")
	private String businessEmail;

	@NotBlank(message = "category is required")
	@Size(max = 100, message = "category must not exceed 100 characters")
	private String category;

	@NotNull(message = "budget is required")
	@DecimalMin(value = "0.00", message = "budget must not be negative")
	private BigDecimal budget;

	@NotBlank(message = "adType is required")
	@Size(max = 100, message = "adType must not exceed 100 characters")
	private String adType;

	@Size(max = 4000, message = "description must not exceed 4000 characters")
	private String description;

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
}
