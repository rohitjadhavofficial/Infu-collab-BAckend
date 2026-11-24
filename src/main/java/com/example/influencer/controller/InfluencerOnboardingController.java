package com.example.influencer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.influencer.dto.ApiResponse;
import com.example.influencer.dto.InfluencerLoginRequest;
import com.example.influencer.dto.InfluencerOnboardingRequest;
import com.example.influencer.exception.DuplicateEmailException;
import com.example.influencer.service.InfluencerOnboardingService;

@RestController
@RequestMapping("/api/influencers")
public class InfluencerOnboardingController {

	private static final Logger log = LoggerFactory.getLogger(InfluencerOnboardingController.class);

	private final InfluencerOnboardingService influencerOnboardingService;

	public InfluencerOnboardingController(InfluencerOnboardingService influencerOnboardingService) {
		this.influencerOnboardingService = influencerOnboardingService;
	}

	@PostMapping(value = "/onboarding", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse> onboard(@RequestBody InfluencerOnboardingRequest request) {
		log.info("DATA Accepted");
		 try {
		        influencerOnboardingService.save(request);

		        return ResponseEntity.ok(
		            ApiResponse.ok("Influencer data saved successfully")
		        );

		    } catch (DuplicateEmailException e) {

		        return ResponseEntity.status(HttpStatus.CONFLICT)
		                .body(ApiResponse.ok("Duplicate email detected"));
		    }
	}

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> login(@RequestBody InfluencerLoginRequest request) {
		return influencerOnboardingService.login(request)
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.ok("WRONG CREDENCTIOLS"));
	}
}
