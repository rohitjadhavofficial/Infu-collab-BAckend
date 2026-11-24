package com.example.influencer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.example.influencer.dto.ImageUploadResponse;
import com.example.influencer.exception.GlobalExceptionHandler;
import com.example.influencer.exception.ImageUploadException;
import com.example.influencer.service.InfluencerImageService;

@WebMvcTest({ InfluencerImageController.class, GlobalExceptionHandler.class })
class InfluencerImageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InfluencerImageService influencerImageService;

	@Test
	void uploadImageReturnsStoredUrl() throws Exception {
		when(influencerImageService.storeImage(eq("rohit.jadhav@example.com"), any(MultipartFile.class)))
				.thenReturn(ImageUploadResponse.stored(
						"http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_1.jpg"));

		MockMultipartFile image = new MockMultipartFile("image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE,
				new byte[] { 1, 2, 3 });

		mockMvc.perform(multipart("/api/influencers/upload-image")
				.file(image)
				.param("email", "rohit.jadhav@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Image stored successfully"))
				.andExpect(jsonPath("$.imageUrl")
						.value("http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_1.jpg"));
	}

	@Test
	void uploadImageReturnsLimitMessage() throws Exception {
		when(influencerImageService.storeImage(eq("rohit.jadhav@example.com"), any(MultipartFile.class)))
				.thenThrow(new ImageUploadException(HttpStatus.BAD_REQUEST, "Cannot upload more than 3 images"));

		MockMultipartFile image = new MockMultipartFile("image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE,
				new byte[] { 1, 2, 3 });

		mockMvc.perform(multipart("/api/influencers/upload-image")
				.file(image)
				.param("email", "rohit.jadhav@example.com"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Cannot upload more than 3 images"))
				.andExpect(jsonPath("$.imageUrl").value((Object) null));
	}

	@Test
	void getImagesReturnsUrlsForEmail() throws Exception {
		when(influencerImageService.listImageUrls("rohit.jadhav@example.com"))
				.thenReturn(List.of(
						"http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_1.png",
						"http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_2.png"));

		mockMvc.perform(get("/api/influencers/images").param("email", "rohit.jadhav@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0]")
						.value("http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_1.png"))
				.andExpect(jsonPath("$[1]")
						.value("http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_2.png"));
	}
}
