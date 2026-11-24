package com.example.influencer.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.influencer.config.UploadPaths;
import com.example.influencer.dto.ImageUploadResponse;
import com.example.influencer.exception.ImageUploadException;

@Service
public class InfluencerImageService {

	private static final Logger log = LoggerFactory.getLogger(InfluencerImageService.class);
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
	private static final byte[] JPEG_MAGIC = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
	private static final byte[] PNG_MAGIC = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] RIFF = { 0x52, 0x49, 0x46, 0x46 };
	private static final byte[] WEBP = { 0x57, 0x45, 0x42, 0x50 };

	private final Path uploadRoot;
	private final int maxImagesPerUser;
	private final long maxFileSizeBytes;
	private final String baseUrl;
	private final ConcurrentHashMap<String, Object> userLocks = new ConcurrentHashMap<>();

	public InfluencerImageService(
			@Value("${app.upload.directory:uploads}") String uploadDirectory,
			@Value("${app.upload.max-images-per-user:3}") int maxImagesPerUser,
			@Value("${app.upload.max-file-size-bytes:5242880}") long maxFileSizeBytes,
			@Value("${app.base-url:http://localhost:8080}") String baseUrl) {
		this.uploadRoot = UploadPaths.resolveRoot(uploadDirectory);
		this.maxImagesPerUser = maxImagesPerUser;
		this.maxFileSizeBytes = maxFileSizeBytes;
		this.baseUrl = trimTrailingSlash(baseUrl);
	}

	public ImageUploadResponse storeImage(String email, MultipartFile image) {
		String normalizedEmail = validateEmail(email);
		byte[] content = readAndValidateImage(image);
		String extension = detectExtension(content);
		String folderName = toSafeFolderName(normalizedEmail);

		Object lock = userLocks.computeIfAbsent(folderName, key -> new Object());
		synchronized (lock) {
			try {
				Path userDirectory = resolveUserDirectory(folderName);
				Files.createDirectories(userDirectory);
				long existingCount = countImages(userDirectory);
				if (existingCount >= maxImagesPerUser) {
					throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Cannot upload more than 3 images");
				}

				String fileName = nextUniqueFileName(userDirectory, extension);
				Path destination = userDirectory.resolve(fileName).normalize();
				if (!destination.startsWith(userDirectory)) {
					throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid image file");
				}
				Files.write(destination, content);

				String imageUrl = toImageUrl(folderName, fileName);
				log.info("Image stored successfully");
				log.info("User email: {}", normalizedEmail);
				log.info("Image URL: {}", imageUrl);
				return ImageUploadResponse.stored(imageUrl);
			} catch (ImageUploadException ex) {
				throw ex;
			} catch (IOException ex) {
				log.error("Failed to store uploaded image");
				throw new ImageUploadException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store image");
			}
		}
	}

	public List<String> listImageUrls(String email) {
		String normalizedEmail = validateEmail(email);
		String folderName = toSafeFolderName(normalizedEmail);
		Path userDirectory = resolveExistingUserDirectory(folderName);
		if (userDirectory == null || !Files.exists(userDirectory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(userDirectory)) {
			return files.filter(Files::isRegularFile)
					.filter(this::hasImageExtension)
					.map(file -> file.getFileName().toString())
					.sorted()
					.map(fileName -> toImageUrl(folderName, fileName))
					.toList();
		} catch (IOException ex) {
			log.error("Failed to list uploaded images");
			throw new ImageUploadException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load images");
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
		if (detectExtension(content) == null) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST,
					"Unsupported image format. Allowed formats: JPG, JPEG, PNG, WEBP");
		}
		validateImageContent(content);
		return content;
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
		if (isWebp(content)) {
			return "webp";
		}
		return null;
	}

	private boolean isWebp(byte[] content) {
		return content.length >= 12 && startsWith(content, RIFF) && containsAt(content, 8, WEBP);
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

	private Path resolveUserDirectory(String folderName) throws IOException {
		Path usersRoot = uploadRoot.resolve("users").normalize();
		Files.createDirectories(usersRoot);
		Path userDirectory = usersRoot.resolve(folderName).normalize();
		if (!userDirectory.startsWith(usersRoot)) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid email address");
		}
		return userDirectory;
	}

	private Path resolveExistingUserDirectory(String folderName) {
		Path usersRoot = uploadRoot.resolve("users").normalize();
		Path userDirectory = usersRoot.resolve(folderName).normalize();
		if (!userDirectory.startsWith(usersRoot)) {
			throw new ImageUploadException(HttpStatus.BAD_REQUEST, "Invalid email address");
		}
		return userDirectory;
	}

	private String toImageUrl(String folderName, String fileName) {
		return baseUrl + "/uploads/users/" + folderName + "/" + fileName;
	}

	private long countImages(Path userDirectory) throws IOException {
		if (!Files.exists(userDirectory)) {
			return 0;
		}
		try (Stream<Path> files = Files.list(userDirectory)) {
			return files.filter(Files::isRegularFile).filter(this::hasImageExtension).count();
		}
	}

	private boolean hasImageExtension(Path file) {
		String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
		int dot = name.lastIndexOf('.');
		if (dot < 0) {
			return false;
		}
		return IMAGE_EXTENSIONS.contains(name.substring(dot + 1));
	}

	private String nextUniqueFileName(Path userDirectory, String extension) {
		int nextIndex = 1;
		String fileName = "image_" + nextIndex + "." + extension;
		while (Files.exists(userDirectory.resolve(fileName))) {
			nextIndex++;
			fileName = "image_" + nextIndex + "." + extension;
		}
		return fileName;
	}

	private String trimTrailingSlash(String value) {
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}
		return value;
	}
}
