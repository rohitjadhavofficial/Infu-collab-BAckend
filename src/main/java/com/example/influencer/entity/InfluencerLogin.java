package com.example.influencer.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.influencer.converter.StringListJsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Influ_login")
public class InfluencerLogin {

	@Id
	@Column(name = "email", nullable = false, length = 255)
	private String email;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "followers")
	private Long followers;

	@Convert(converter = StringListJsonConverter.class)
	@Column(name = "categories")
	private List<String> categories;

	@Column(name = "averageCollaborationPriceInr", precision = 12, scale = 2)
	private BigDecimal averageCollaborationPriceInr;

	@Column(name = "location")
	private String location;

	@Column(name = "countryCode", length = 10)
	private String countryCode;

	@Column(name = "mobile", length = 20)
	private String mobile;

	@Column(name = "password", length = 255)
	private String password;

	@Column(name = "submittedAt")
	private Instant submittedAt;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

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
