package com.example.influencer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BusinessOnboardingRequest {

	@NotBlank(message = "businessName is required")
	@Size(max = 255, message = "businessName must not exceed 255 characters")
	private String businessName;

	@NotBlank(message = "category is required")
	@Size(max = 100, message = "category must not exceed 100 characters")
	private String category;

	@NotBlank(message = "city is required")
	@Size(max = 100, message = "city must not exceed 100 characters")
	private String city;

	@NotBlank(message = "email is required")
	@Email(message = "email must be a valid email address")
	@Size(max = 255, message = "email must not exceed 255 characters")
	private String email;

	@NotBlank(message = "phone is required")
	@Size(max = 20, message = "phone must not exceed 20 characters")
	@Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "phone must be a valid phone number")
	private String phone;

	@NotBlank(message = "password is required")
	@Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
	private String password;

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
