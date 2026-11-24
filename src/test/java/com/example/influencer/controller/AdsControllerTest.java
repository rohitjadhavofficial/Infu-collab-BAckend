package com.example.influencer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.influencer.dto.AdsRequest;
import com.example.influencer.dto.AdsResponse;
import com.example.influencer.exception.GlobalExceptionHandler;
import com.example.influencer.service.AdsService;

@WebMvcTest({ AdsController.class, GlobalExceptionHandler.class })
class AdsControllerTest {

	private static final String VALID_PAYLOAD = """
			{
			  "adTitle": "Festive Season Electronics Sale",
			  "businessName": "Shree Electronics",
			  "businessEmail": "business@gmail.com",
			  "category": "Electronics",
			  "budget": 5000.00,
			  "adType": "Instagram Reel",
			  "description": "Promote our electronics store and festive season discounts."
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdsService adsService;

	@Test
	void createReturnsCreatedAdvertisementWithAdTitle() throws Exception {
		AdsResponse response = new AdsResponse();
		response.setId(1L);
		response.setAdTitle("Festive Season Electronics Sale");
		response.setBusinessName("Shree Electronics");
		response.setBusinessEmail("business@gmail.com");
		response.setCategory("Electronics");
		response.setBudget(new BigDecimal("5000.00"));
		response.setAdType("Instagram Reel");
		response.setDescription("Promote our electronics store and festive season discounts.");
		response.setCreatedAt(Instant.parse("2026-10-25T20:30:00Z"));
		when(adsService.create(any(AdsRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/ads")
				.contentType(MediaType.APPLICATION_JSON)
				.content(VALID_PAYLOAD))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.adTitle").value("Festive Season Electronics Sale"))
				.andExpect(jsonPath("$.businessName").value("Shree Electronics"))
				.andExpect(jsonPath("$.businessEmail").value("business@gmail.com"))
				.andExpect(jsonPath("$.category").value("Electronics"))
				.andExpect(jsonPath("$.budget").value(5000.00))
				.andExpect(jsonPath("$.adType").value("Instagram Reel"))
				.andExpect(jsonPath("$.description")
						.value("Promote our electronics store and festive season discounts."));
	}

	@Test
	void createReturnsBadRequestWhenAdTitleIsBlank() throws Exception {
		mockMvc.perform(post("/api/ads")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "adTitle": "   ",
						  "businessName": "Shree Electronics",
						  "businessEmail": "business@gmail.com",
						  "category": "Electronics",
						  "budget": 5000.00,
						  "adType": "Instagram Reel",
						  "description": "Promote our electronics store and festive season discounts."
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isString());
	}

	@Test
	void createReturnsBadRequestWhenAdTitleIsMissing() throws Exception {
		mockMvc.perform(post("/api/ads")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "businessName": "Shree Electronics",
						  "businessEmail": "business@gmail.com",
						  "category": "Electronics",
						  "budget": 5000.00,
						  "adType": "Instagram Reel",
						  "description": "Promote our electronics store and festive season discounts."
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isString());
	}

	@Test
	void getAllAdsReturnsAdvertisements() throws Exception {
		AdsResponse response = new AdsResponse();
		response.setId(1L);
		response.setAdTitle("Festive Season Electronics Sale");
		response.setBusinessName("Shree Electronics");
		response.setBusinessEmail("business@gmail.com");
		response.setCategory("Electronics");
		response.setBudget(new BigDecimal("5000.00"));
		response.setAdType("Instagram Reel");
		when(adsService.listAll()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/ads"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].adTitle").value("Festive Season Electronics Sale"))
				.andExpect(jsonPath("$[0].businessName").value("Shree Electronics"))
				.andExpect(jsonPath("$[0].businessEmail").value("business@gmail.com"));
	}

	@Test
	void createReturnsBadRequestWhenBudgetIsNegative() throws Exception {
		mockMvc.perform(post("/api/ads")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "adTitle": "Festive Season Electronics Sale",
						  "businessName": "Shree Electronics",
						  "businessEmail": "business@gmail.com",
						  "category": "Electronics",
						  "budget": -10.00,
						  "adType": "Instagram Reel",
						  "description": "Promote our electronics store and festive season discounts."
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isString());
	}

	@Test
	void createReturnsBadRequestWhenBusinessEmailIsInvalid() throws Exception {
		mockMvc.perform(post("/api/ads")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "adTitle": "Festive Season Electronics Sale",
						  "businessName": "Shree Electronics",
						  "businessEmail": "not-an-email",
						  "category": "Electronics",
						  "budget": 5000.00,
						  "adType": "Instagram Reel",
						  "description": "Promote our electronics store and festive season discounts."
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isString());
	}
}
