package com.example.influencer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.example.influencer.dto.ImageUploadResponse;
import com.example.influencer.exception.ImageUploadException;

class InfluencerImageServiceTest {

	@TempDir
	Path tempDir;

	private InfluencerImageService influencerImageService;

	@BeforeEach
	void setUp() {
		influencerImageService = new InfluencerImageService(tempDir.toString(), 3, 5_242_880, "http://localhost:8080");
	}

	@Test
	void storeImageSavesFileAndReturnsAccessibleUrl() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", pngBytes());

		ImageUploadResponse response = influencerImageService.storeImage("Rohit.Jadhav@example.com", image);

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.getMessage()).isEqualTo("Image stored successfully");
		assertThat(response.getImageUrl())
				.isEqualTo("http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_1.png");
		assertThat(Files.exists(tempDir.resolve("users").resolve("rohit.jadhav@example.com").resolve("image_1.png")))
				.isTrue();
	}

	@Test
	void storeImageRejectsFourthImage() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", pngBytes());
		influencerImageService.storeImage("rohit.jadhav@example.com", image);
		influencerImageService.storeImage("rohit.jadhav@example.com", image);
		influencerImageService.storeImage("rohit.jadhav@example.com", image);

		assertThatThrownBy(() -> influencerImageService.storeImage("rohit.jadhav@example.com", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Cannot upload more than 3 images")
				.extracting(ex -> ((ImageUploadException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void storeImageRejectsInvalidEmail() {
		MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", new byte[] { 1, 2, 3 });

		assertThatThrownBy(() -> influencerImageService.storeImage("not-an-email", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Invalid email address");
	}

	@Test
	void storeImageRejectsNonImageContent() {
		MockMultipartFile image = new MockMultipartFile("image", "notes.txt", "text/plain", "hello".getBytes());

		assertThatThrownBy(() -> influencerImageService.storeImage("rohit.jadhav@example.com", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
	}

	@Test
	void listImageUrlsReturnsStoredImageLinks() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", pngBytes());
		influencerImageService.storeImage("rohit.jadhav@example.com", image);
		influencerImageService.storeImage("rohit.jadhav@example.com", image);

		assertThat(influencerImageService.listImageUrls("Rohit.Jadhav@example.com")).containsExactly(
				"http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_1.png",
				"http://localhost:8080/uploads/users/rohit.jadhav@example.com/image_2.png");
	}

	@Test
	void listImageUrlsReturnsEmptyListWhenUserHasNoImages() {
		assertThat(influencerImageService.listImageUrls("missing.user@example.com")).isEmpty();
	}

	private byte[] pngBytes() throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}
}
