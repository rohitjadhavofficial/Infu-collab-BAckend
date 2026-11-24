package com.example.influencer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class InfluencerOnboardingRequest {

	private String name;
	private Long followers;
	private List<String> categories;
	private BigDecimal averageCollaborationPriceInr;
	private String location;
	private String email;
	private String countryCode;
	private String mobile;
	private String password;
	private Instant submittedAt;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getFollowers() {
		return followers;
	}

	public void setFollowers(Long followers) {
		this.followers = followers;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public BigDecimal getAverageCollaborationPriceInr() {
		return averageCollaborationPriceInr;
	}

	public void setAverageCollaborationPriceInr(BigDecimal averageCollaborationPriceInr) {
		this.averageCollaborationPriceInr = averageCollaborationPriceInr;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Instant getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(Instant submittedAt) {
		this.submittedAt = submittedAt;
	}
}
