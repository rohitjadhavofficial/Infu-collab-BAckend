package com.example.influencer.service;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.influencer.dto.AdsRequest;
import com.example.influencer.dto.AdsResponse;
import com.example.influencer.entity.Ads;
import com.example.influencer.repository.AdsRepository;

@Service
public class AdsService {

	private static final Logger log = LoggerFactory.getLogger(AdsService.class);

	private final AdsRepository adsRepository;

	public AdsService(AdsRepository adsRepository) {
		this.adsRepository = adsRepository;
	}

	@Transactional
	public AdsResponse create(AdsRequest request) {
		Ads advertisement = new Ads();
		advertisement.setAdTitle(request.getAdTitle().trim());
		advertisement.setBusinessName(request.getBusinessName().trim());
		advertisement.setBusinessEmail(request.getBusinessEmail().trim().toLowerCase(Locale.ROOT));
		advertisement.setCategory(request.getCategory().trim());
		advertisement.setBudget(request.getBudget());
		advertisement.setAdType(request.getAdType().trim());
		advertisement.setDescription(trimToNull(request.getDescription()));

		Ads saved = adsRepository.saveAndFlush(advertisement);
		log.info("Advertisement saved with id {}", saved.getId());
		return AdsResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<AdsResponse> listAll() {
		return adsRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(AdsResponse::from)
				.toList();
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
