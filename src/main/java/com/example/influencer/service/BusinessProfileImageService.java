package com.example.influencer.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.influencer.config.UploadPaths;
import com.example.influencer.dto.BusinessProfileImageResponse;
import com.example.influencer.exception.ImageUploadException;

@Service
public class BusinessProfileImageService {

	private static final Logger log = LoggerFactory.getLogger(BusinessProfileImageService.class);
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
	private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/pjpeg",
			"image/png", "image/webp", "application/octet-stream");
	private static final byte[] JPEG_MAGIC = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
	private static final byte[] PNG_MAGIC = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] RIFF = { 0x52, 0x49, 0x46, 0x46 };
	private static final byte[] WEBP = { 0x57, 0x45, 0x42, 0x50 };

	private final Path businessRoot;
	private final long maxFileSizeBytes;
	private final String baseUrl;
	private final ConcurrentHashMap<String, Object> businessLocks = new ConcurrentHashMap<>();

	public BusinessProfileImageService(
			@Value("${app.business.upload.directory:Bussness}") String uploadDirectory,
			@Value("${app.upload.max-file-size-bytes:5242880}") long maxFileSizeBytes,
			@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
		this.businessRoot = UploadPaths.resolveRoot(uploadDirectory);
		this.maxFileSizeBytes = maxFileSizeBytes;
		this.baseUrl = trimTrailingSlash(baseUrl);
	}

	public BusinessProfileImageResponse storeProfileImage(String email, MultipartFile image) {
		String normalizedEmail = validateEmail(email);
		byte[] content = readAndValidateImage(image);
		String extension = resolveStoredExtension(image, content);
		String folderName = toSafeFolderName(normalizedEmail);

		Object lock = businessLocks.computeIfAbsent(folderName, key -> new Object());
		synchronized (lock) {
			try {
				Path businessDirectory = resolveBusinessDirectory(folderName);
				Files.createDirectories(businessDirectory);

				String fileName = "profile." + extension;
				Path destination = businessDirectory.resolve(fileName).normalize();
				if (!destination.startsWith(businessDirectory)) {
					throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid image file");
				}

				deleteExistingProfileImages(businessDirectory, destination);
				try (InputStream input = new ByteArrayInputStream(content)) {
					Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
				}

				log.info("Business profile image uploaded successfully");
				log.info("Business email: {}", normalizedEmail);
				return BusinessProfileImageResponse.stored(normalizedEmail, fileName);
			} catch (ImageUploadException ex) {
				throw ex;
			} catch (IOException ex) {
				log.error("Failed to store business profile image");
				throw new ImageUploadException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store image");
			}
		}
	}

	public String getProfileImageUrl(String email) {
		String normalizedEmail = validateEmail(email);
		String folderName = toSafeFolderName(normalizedEmail);
		Path businessDirectory = resolveExistingBusinessDirectory(folderName);
		if (businessDirectory == null || !Files.exists(businessDirectory) || !Files.isDirectory(businessDirectory)) {
			return "";
		}
		try (DirectoryStream<Path> files = Files.newDirectoryStream(businessDirectory)) {
			for (Path file : files) {
				Path normalized = file.toAbsolutePath().normalize();
				if (!normalized.startsWith(businessDirectory) || !Files.isRegularFile(normalized)) {
					continue;
				}
				String fileName = normalized.getFileName().toString();
				if (isProfileImage(fileName)) {
					return toImageUrl(folderName, fileName);
				}
			}
			return "";
		} catch (IOException ex) {
			log.error("Failed to load business profile image");
			throw new ImageUploadException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load image");
		}
	}

	private void deleteExistingProfileImages(Path businessDirectory, Path destination) throws IOException {
		try (DirectoryStream<Path> files = Files.newDirectoryStream(businessDirectory)) {
			for (Path file : files) {
				Path normalized = file.toAbsolutePath().normalize();
				if (!normalized.startsWith(businessDirectory) || Files.isDirectory(normalized)
						|| normalized.equals(destination)) {
					continue;
				}
				Files.deleteIfExists(normalized);
			}
		}
	}

	private String validateEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Email is required");
		}
		String normalized = email.trim().toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(normalized).matches()) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid email address");
		}
		return normalized;
	}

	private byte[] readAndValidateImage(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Image is required");
		}
		if (image.getSize() > maxFileSizeBytes) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Image file exceeds the maximum allowed size");
		}
		byte[] content;
		try {
			content = image.getBytes();
		} catch (IOException ex) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid image file");
		}
		if (content.length == 0) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Image is required");
		}
		validateDeclaredMetadata(image);
		if (detectExtension(content) == null) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST,
					"Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
		}
		validateImageContent(content);
		return content;
	}

	private void validateDeclaredMetadata(MultipartFile image) {
		String declaredExtension = originalExtension(image.getOriginalFilename());
		if (declaredExtension != null && !IMAGE_EXTENSIONS.contains(declaredExtension)) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST,
					"Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
		}
		String contentType = image.getContentType();
		if (contentType != null && !contentType.isBlank()) {
			String normalizedType = contentType.trim().toLowerCase(Locale.ROOT);
			int separator = normalizedType.indexOf(';');
			if (separator >= 0) {
				normalizedType = normalizedType.substring(0, separator).trim();
			}
			if (!IMAGE_CONTENT_TYPES.contains(normalizedType)) {
				throw new ImageUploadException(HttpStatus.BAD_REQUEST,
						"Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
			}
		}
	}

	private String resolveStoredExtension(MultipartFile image, byte[] content) {
		String detected = detectExtension(content);
		String declared = originalExtension(image.getOriginalFilename());
		if (declared == null) {
			return detected;
		}
		if ("jpg".equals(detected) && ("jpg".equals(declared) || "jpeg".equals(declared))) {
			return declared;
		}
		if (!detected.equals(declared)) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST,
					"Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
		}
		return detected;
	}

	private String originalExtension(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return null;
		}
		try {
			String name = Path.of(originalFilename).getFileName().toString();
			int dot = name.lastIndexOf('.');
			if (dot < 0 || dot == name.length() - 1) {
				return null;
			}
			return name.substring(dot + 1).toLowerCase(Locale.ROOT);
		} catch (RuntimeException ex) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid image file");
		}
	}

	private void validateImageContent(byte[] content) {
		String extension = detectExtension(content);
		if ("webp".equals(extension)) {
			return;
		}
		try (InputStream input = new ByteArrayInputStream(content)) {
			BufferedImage decoded = ImageIO.read(input);
			if (decoded == null) {
				throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid image file");
			}
		} catch (ImageUploadException ex) {
			throw ex;
		} catch (IOException ex) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid image file");
		}
	}

	private String detectExtension(byte[] content) {
		if (startsWith(content, JPEG_MAGIC)) {
			return "jpg";
		}
		if (startsWith(content, PNG_MAGIC)) {
			return "png";
		}
		if (content.length >= 12 && startsWith(content, RIFF) && containsAt(content, 8, WEBP)) {
			return "webp";
		}
		return null;
	}

	private boolean startsWith(byte[] content, byte[] magic) {
		return containsAt(content, 0, magic);
	}

	private boolean containsAt(byte[] content, int offset, byte[] magic) {
		if (content.length < offset + magic.length) {
			return false;
		}
		for (int i = 0; i < magic.length; i++) {
			if (content[offset + i] != magic[i]) {
				return false;
			}
		}
		return true;
	}

	private String toSafeFolderName(String email) {
		String folderName = email.replaceAll("[<>:\"/\\\\|?*]", "_");
		if (folderName.contains("..") || folderName.isBlank()) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid email address");
		}
		return folderName;
	}

	private Path resolveBusinessDirectory(String folderName) throws IOException {
		Files.createDirectories(businessRoot);
		Path businessDirectory = businessRoot.resolve(folderName).normalize();
		if (!businessDirectory.startsWith(businessRoot)) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid email address");
		}
		return businessDirectory;
	}

	private Path resolveExistingBusinessDirectory(String folderName) {
		Path businessDirectory = businessRoot.resolve(folderName).normalize();
		if (!businessDirectory.startsWith(businessRoot)) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid email address");
		}
		return businessDirectory;
	}

	private boolean isProfileImage(String fileName) {
		String name = fileName.toLowerCase(Locale.ROOT);
		int dot = name.lastIndexOf('.');
		if (dot < 0 || !name.startsWith("profile.")) {
			return false;
		}
		return IMAGE_EXTENSIONS.contains(name.substring(dot + 1));
	}

	private String toImageUrl(String folderName, String fileName) {
		return baseUrl + "/Bussness/" + folderName + "/" + fileName;
	}

	private String trimTrailingSlash(String value) {
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}
		return value;
	}
}
