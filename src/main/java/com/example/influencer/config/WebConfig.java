package com.example.influencer.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

	private final String uploadDirectory;
	private final String businessUploadDirectory;

	public WebConfig(
			@Value("${app.upload.directory:uploads}") String uploadDirectory,
			@Value("${app.business.upload.directory:Bussness}") String businessUploadDirectory) {
		this.uploadDirectory = uploadDirectory;
		this.businessUploadDirectory = businessUploadDirectory;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registerDirectory(registry, "/uploads/**", uploadDirectory, "Unable to initialize upload directory");
		registerDirectory(registry, "/Bussness/**", businessUploadDirectory,
				"Unable to initialize business upload directory");
	}

	private void registerDirectory(ResourceHandlerRegistry registry, String handlerPattern, String directory,
			String errorMessage) {
		Path root = UploadPaths.resolveRoot(directory);
		try {
			Files.createDirectories(root);
		} catch (IOException ex) {
			log.error(errorMessage);
		}
		registry.addResourceHandler(handlerPattern)
				.addResourceLocations(root.toUri().toString())
				.setCachePeriod(0);
	}
}
