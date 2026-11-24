package com.example.influencer.config;

import java.nio.file.Path;

public final class UploadPaths {

	private UploadPaths() {
	}

	public static Path resolveRoot(String directory) {
		Path root = Path.of(directory);
		if (!root.isAbsolute()) {
			root = Path.of(System.getProperty("user.dir")).resolve(directory);
		}
		return root.toAbsolutePath().normalize();
	}
}
