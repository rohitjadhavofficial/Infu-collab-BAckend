package com.example.influencer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.influencer.dto.InfluencerResponseDTO;
import com.example.influencer.service.InfluencerOnboardingService;

@RestController
public class InfluencerListController {

	private final InfluencerOnboardingService influencerOnboardingService;

	public InfluencerListController(InfluencerOnboardingService influencerOnboardingService) {
		this.influencerOnboardingService = influencerOnboardingService;
	}

	@GetMapping({ "/influencers", "/api/influencers" })
	public ResponseEntity<List<InfluencerResponseDTO>> getAllInfluencers() {
		return ResponseEntity.ok(influencerOnboardingService.listAllWithProfileImages());
	}
}
