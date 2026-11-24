package com.example.influencer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.example.influencer.dto.BusinessProfileImageResponse;
import com.example.influencer.exception.ImageUploadException;

class BusinessProfileImageServiceTest {

	@TempDir
	Path tempDir;

	private BusinessProfileImageService businessProfileImageService;

	@BeforeEach
	void setUp() {
		businessProfileImageService = new BusinessProfileImageService(tempDir.toString(), 5_242_880,
				"http://localhost:8080");
	}

	@Test
	void storeProfileImageCreatesEmailFolderAndSavesProfileFile() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", pngBytes());

		BusinessProfileImageResponse response = businessProfileImageService.storeProfileImage("Business1@gmail.com",
				image);

		assertThat(response.isSuccess()).isTrue();
		assertThat(response.getEmail()).isEqualTo("business1@gmail.com");
		assertThat(response.getFileName()).isEqualTo("profile.png");
		assertThat(Files.exists(tempDir.resolve("business1@gmail.com").resolve("profile.png"))).isTrue();
	}

	@Test
	void storeProfileImageReplacesExistingFileAndKeepsOnlyOneImage() throws Exception {
		MockMultipartFile png = new MockMultipartFile("image", "photo.png", "image/png", pngBytes());
		MockMultipartFile jpg = new MockMultipartFile("image", "photo.jpg", "image/jpeg", jpegBytes());

		businessProfileImageService.storeProfileImage("business1@gmail.com", png);
		businessProfileImageService.storeProfileImage("business1@gmail.com", jpg);

		Path businessDir = tempDir.resolve("business1@gmail.com");
		try (Stream<Path> files = Files.list(businessDir)) {
			List<String> names = files.map(file -> file.getFileName().toString()).toList();
			assertThat(names).containsExactly("profile.jpg");
		}
		assertThat(Files.exists(businessDir.resolve("profile.png"))).isFalse();
	}

	@Test
	void storeProfileImageDoesNotAffectAnotherBusiness() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", pngBytes());
		businessProfileImageService.storeProfileImage("business1@gmail.com", image);
		businessProfileImageService.storeProfileImage("business2@gmail.com", image);

		assertThat(Files.exists(tempDir.resolve("business1@gmail.com").resolve("profile.png"))).isTrue();
		assertThat(Files.exists(tempDir.resolve("business2@gmail.com").resolve("profile.png"))).isTrue();
	}

	@Test
	void storeProfileImageRejectsEmptyFile() {
		MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", new byte[0]);

		assertThatThrownBy(() -> businessProfileImageService.storeProfileImage("business1@gmail.com", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Image is required")
				.extracting(ex -> ((ImageUploadException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void storeProfileImageRejectsUnsupportedType() {
		MockMultipartFile image = new MockMultipartFile("image", "notes.txt", "text/plain", "hello".getBytes());

		assertThatThrownBy(() -> businessProfileImageService.storeProfileImage("business1@gmail.com", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
	}

	@Test
	void storeProfileImageRejectsInvalidEmail() {
		MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", new byte[] { 1, 2, 3 });

		assertThatThrownBy(() -> businessProfileImageService.storeProfileImage("not-an-email", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Invalid email address");
	}

	@Test
	void storeProfileImageRejectsBlankEmail() {
		MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", new byte[] { 1, 2, 3 });

		assertThatThrownBy(() -> businessProfileImageService.storeProfileImage("  ", image))
				.isInstanceOf(ImageUploadException.class)
				.hasMessage("Email is required");
	}

	@Test
	void getProfileImageUrlReturnsStoredImageUrl() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "photo.png", "image/png", pngBytes());
		businessProfileImageService.storeProfileImage("business1@gmail.com", image);

		assertThat(businessProfileImageService.getProfileImageUrl("Business1@gmail.com"))
				.isEqualTo("http://localhost:8080/Bussness/business1@gmail.com/profile.png");
	}

	@Test
	void getProfileImageUrlReturnsEmptyWhenNoImageExists() {
		assertThat(businessProfileImageService.getProfileImageUrl("missing@gmail.com")).isEmpty();
	}

	@Test
	void storeProfileImagePreservesJpegExtension() throws Exception {
		MockMultipartFile image = new MockMultipartFile("image", "photo.jpeg", "image/jpeg", jpegBytes());

		BusinessProfileImageResponse response = businessProfileImageService.storeProfileImage("business1@gmail.com",
				image);

		assertThat(response.getFileName()).isEqualTo("profile.jpeg");
		assertThat(Files.exists(tempDir.resolve("business1@gmail.com").resolve("profile.jpeg"))).isTrue();
	}

	private byte[] pngBytes() throws IOException {
		return imageBytes("png");
	}

	private byte[] jpegBytes() throws IOException {
		return imageBytes("jpg");
	}

	private byte[] imageBytes(String format) throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, format, output);
		return output.toByteArray();
	}
}
