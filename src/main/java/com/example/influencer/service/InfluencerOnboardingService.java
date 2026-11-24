package com.example.influencer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import com.example.influencer.dto.InfluencerLoginRequest;
import com.example.influencer.dto.InfluencerOnboardingRequest;
import com.example.influencer.dto.InfluencerProfileResponse;
import com.example.influencer.dto.InfluencerResponseDTO;
import com.example.influencer.entity.InfluencerLogin;
import com.example.influencer.exception.DuplicateEmailException;
import com.example.influencer.repository.InfluencerLoginRepository;

@Service
public class InfluencerOnboardingService {

	private static final Logger log = LoggerFactory.getLogger(InfluencerOnboardingService.class);

	private final InfluencerLoginRepository influencerLoginRepository;
	private final InfluencerImageService influencerImageService;

	public InfluencerOnboardingService(InfluencerLoginRepository influencerLoginRepository,
			InfluencerImageService influencerImageService) {
		this.influencerLoginRepository = influencerLoginRepository;
		this.influencerImageService = influencerImageService;
	}

	@Transactional
	public void save(InfluencerOnboardingRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (influencerLoginRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateEmailException();
		}

		InfluencerLogin influencer = toEntity(request, email);
		try {
			influencerLoginRepository.saveAndFlush(influencer);
			log.info("Influencer onboarding data saved");
		} catch (DataIntegrityViolationException ex) {
			throw new DuplicateEmailException();
		}
	}

	@Transactional(readOnly = true)
	public Optional<InfluencerProfileResponse> login(InfluencerLoginRequest request) {
		String email = normalizeEmail(request.getEmail());
		String password = request.getPassword();
		if (email == null || email.isBlank() || password == null) {
			return Optional.empty();
		}

		return influencerLoginRepository.findByEmailIgnoreCase(email)
				.filter(influencer -> password.equals(influencer.getPassword()))
				.map(InfluencerProfileResponse::from);
	}

	@Transactional(readOnly = true)
	public List<InfluencerResponseDTO> listAllWithProfileImages() {
		return influencerLoginRepository.findAll().stream()
				.map(influencer -> InfluencerResponseDTO.from(influencer, resolveProfileImageUrl(influencer.getEmail())))
				.toList();
	}

	private String resolveProfileImageUrl(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		try {
			return influencerImageService.listImageUrls(email).stream()
					.filter(this::isProfileImageUrl)
					.findFirst()
					.orElse(null);
		} catch (RuntimeException ex) {
			log.warn("Unable to load profile image for influencer");
			return null;
		}
	}

	private boolean isProfileImageUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return false;
		}
		int separator = imageUrl.lastIndexOf('/');
		String fileName = separator >= 0 ? imageUrl.substring(separator + 1) : imageUrl;
		return fileName.contains("_1.") || fileName.matches(".*_1\\.[^.]+$");
	}

	private InfluencerLogin toEntity(InfluencerOnboardingRequest request, String email) {
		InfluencerLogin influencer = new InfluencerLogin();
		influencer.setEmail(email);
		influencer.setName(request.getName());
		influencer.setFollowers(request.getFollowers());
		influencer.setCategories(request.getCategories());
		influencer.setAverageCollaborationPriceInr(request.getAverageCollaborationPriceInr());
		influencer.setLocation(request.getLocation());
		influencer.setCountryCode(request.getCountryCode());
		influencer.setMobile(request.getMobile());
		influencer.setPassword(request.getPassword());
		influencer.setSubmittedAt(request.getSubmittedAt());
		return influencer;
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase();
	}
}
