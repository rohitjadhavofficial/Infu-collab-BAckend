package com.example.influencer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.influencer.dto.AdsRequest;
import com.example.influencer.dto.AdsResponse;
import com.example.influencer.service.AdsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ads")
public class AdsController {

	private final AdsService adsService;

	public AdsController(AdsService adsService) {
		this.adsService = adsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AdsResponse> create(@Valid @RequestBody AdsRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adsService.create(request));
	}

	@GetMapping
	public ResponseEntity<List<AdsResponse>> getAllAds() {
		return ResponseEntity.ok(adsService.listAll());
	}
}
