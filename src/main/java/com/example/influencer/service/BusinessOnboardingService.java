package com.example.influencer.service;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.influencer.dto.BusinessLoginRequest;
import com.example.influencer.dto.BusinessOnboardingRequest;
import com.example.influencer.dto.BusinessProfileResponse;
import com.example.influencer.entity.BusinessLogin;
import com.example.influencer.exception.BusinessEmailExistsException;
import com.example.influencer.repository.BusinessLoginRepository;

@Service
public class BusinessOnboardingService {

	private static final Logger log = LoggerFactory.getLogger(BusinessOnboardingService.class);

	private final BusinessLoginRepository businessLoginRepository;
	private final PasswordEncoder passwordEncoder;

	public BusinessOnboardingService(BusinessLoginRepository businessLoginRepository,
			PasswordEncoder passwordEncoder) {
		this.businessLoginRepository = businessLoginRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void register(BusinessOnboardingRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (businessLoginRepository.existsByEmailIgnoreCase(email)) {
			throw new BusinessEmailExistsException();
		}

		BusinessLogin business = toEntity(request, email);
		try {
			businessLoginRepository.saveAndFlush(business);
			log.info("Business onboarding data saved");
		} catch (DataIntegrityViolationException ex) {
			throw new BusinessEmailExistsException();
		}
	}

	@Transactional(readOnly = true)
	public Optional<BusinessProfileResponse> login(BusinessLoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		String password = request.getPassword();
		if (email == null || email.isBlank() || password == null || password.isBlank()) {
			return Optional.empty();
		}

		return businessLoginRepository.findByEmailIgnoreCase(email)
				.filter(business -> passwordEncoder.matches(password, business.getPassword()))
				.map(BusinessProfileResponse::from);
	}

	private BusinessLogin toEntity(BusinessOnboardingRequest request, String email) {
		BusinessLogin business = new BusinessLogin();
		business.setBusinessName(request.getBusinessName().trim());
		business.setCategory(request.getCategory().trim());
		business.setCity(request.getCity().trim());
		business.setEmail(email);
		business.setPhone(request.getPhone().trim());
		business.setPassword(passwordEncoder.encode(request.getPassword()));
		return business;
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
