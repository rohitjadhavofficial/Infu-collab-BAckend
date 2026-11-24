package com.example.influencer.dto;

import com.example.influencer.entity.BusinessLogin;

public class BusinessProfileResponse {

	private Long id;
	private String businessName;
	private String category;
	private String city;
	private String email;
	private String phone;

	public static BusinessProfileResponse from(BusinessLogin business) {
		BusinessProfileResponse response = new BusinessProfileResponse();
		response.setId(business.getId());
		response.setBusinessName(business.getBusinessName());
		response.setCategory(business.getCategory());
		response.setCity(business.getCity());
		response.setEmail(business.getEmail());
		response.setPhone(business.getPhone());
		return response;
	}

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
}
