package com.example.influencer.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.influencer.dto.AdsRequest;
import com.example.influencer.dto.AdsResponse;
import com.example.influencer.entity.Ads;
import com.example.influencer.repository.AdsRepository;

@SpringBootTest
@Transactional
class AdsServiceTest {

	@Autowired
	private AdsService adsService;

	@Autowired
	private AdsRepository adsRepository;

	@Test
	void createPersistsAdvertisementTitleAndReturnsIt() {
		AdsRequest request = new AdsRequest();
		request.setAdTitle("Festive Season Electronics Sale");
		request.setBusinessName("Shree Electronics");
		request.setBusinessEmail("Business@gmail.com");
		request.setCategory("Electronics");
		request.setBudget(new BigDecimal("5000.00"));
		request.setAdType("Instagram Reel");
		request.setDescription("Promote our electronics store and festive season discounts.");

		AdsResponse response = adsService.create(request);

		assertThat(response.getId()).isNotNull();
		assertThat(response.getAdTitle()).isEqualTo("Festive Season Electronics Sale");
		assertThat(response.getBusinessName()).isEqualTo("Shree Electronics");
		assertThat(response.getBusinessEmail()).isEqualTo("business@gmail.com");
		assertThat(response.getBudget()).isEqualByComparingTo("5000.00");
		assertThat(response.getCreatedAt()).isNotNull();

		Ads saved = adsRepository.findById(response.getId()).orElseThrow();
		assertThat(saved.getAdTitle()).isEqualTo("Festive Season Electronics Sale");
		assertThat(saved.getBusinessEmail()).isEqualTo("business@gmail.com");
		assertThat(saved.getAdType()).isEqualTo("Instagram Reel");
		assertThat(saved.getDescription()).isEqualTo("Promote our electronics store and festive season discounts.");
	}

	@Test
	void listAllReturnsSavedAdvertisements() {
		adsService.create(sampleRequest("Festive Season Electronics Sale"));
		adsService.create(sampleRequest("Weekend Fashion Promo"));

		assertThat(adsService.listAll())
				.extracting(AdsResponse::getAdTitle)
				.contains("Festive Season Electronics Sale", "Weekend Fashion Promo");
	}

	private AdsRequest sampleRequest(String adTitle) {
		AdsRequest request = new AdsRequest();
		request.setAdTitle(adTitle);
		request.setBusinessName("Shree Electronics");
		request.setBusinessEmail("business@gmail.com");
		request.setCategory("Electronics");
		request.setBudget(new BigDecimal("5000.00"));
		request.setAdType("Instagram Reel");
		request.setDescription("Promote our electronics store and festive season discounts.");
		return request;
	}
}
