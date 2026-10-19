package com.example.mohit.Ecomm.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReturnEvidenceStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    @Value("${shopsphere.uploads.returns-dir:uploads/returns}")
    private String uploadDirectory;

    // =====================================================
    // STORE RETURN EVIDENCE IMAGE
    // =====================================================

    public String store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Image size must not exceed 5 MB.");
        }

        String contentType = file.getContentType();

        String extension;

        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            extension = ".jpg";
        } else if ("image/png".equalsIgnoreCase(contentType)) {
            extension = ".png";
        } else {
            throw new IllegalArgumentException(
                    "Only JPG and PNG images are allowed.");
        }

        Path directory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        String filename = UUID.randomUUID() + extension;

        Path destination = directory.resolve(filename).normalize();

        if (!destination.startsWith(directory)) {
            throw new IllegalArgumentException(
                    "Invalid image destination.");
        }

        try {
            byte[] imageBytes = file.getBytes();

            // Validate that the uploaded content is a real image.
            BufferedImage image = ImageIO.read(
                    new ByteArrayInputStream(imageBytes));

            if (image == null) {
                throw new IllegalArgumentException(
                        "The uploaded file is not a valid image.");
            }

            if (image.getWidth() > 8000
                    || image.getHeight() > 8000) {

                throw new IllegalArgumentException(
                        "Image dimensions must not exceed 8000 × 8000 pixels.");
            }

            Files.createDirectories(directory);

            Files.write(
                    destination,
                    imageBytes,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);

            return filename;

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to save the return evidence image.", ex);
        }
    }

    // =====================================================
    // LOAD EVIDENCE IMAGE
    // =====================================================

    public Resource loadAsResource(String filename) {

        if (filename == null
                || filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.contains("..")) {

            throw new IllegalArgumentException(
                    "Invalid evidence filename.");
        }

        Path directory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        Path file = directory.resolve(filename).normalize();

        if (!file.startsWith(directory)) {
            throw new IllegalArgumentException(
                    "Invalid evidence path.");
        }

        try {
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new IllegalArgumentException(
                    "Evidence image not found.");

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to load evidence image.", ex);
        }
    }

    // =====================================================
    // DELETE IMAGE IF DATABASE SAVE FAILS
    // =====================================================

    public void delete(String filename) {

        if (filename == null || filename.isBlank()) {
            return;
        }

        if (filename.contains("/")
                || filename.contains("\\")
                || filename.contains("..")) {
            return;
        }

        Path directory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        Path file = directory.resolve(filename).normalize();

        if (!file.startsWith(directory)) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to delete the evidence image.", ex);
        }
    }

    // =====================================================
    // GET IMAGE CONTENT TYPE
    // =====================================================

    public String getContentType(String filename) {

        String name = filename.toLowerCase(Locale.ROOT);

        if (name.endsWith(".png")) {
            return "image/png";
        }

        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        throw new IllegalArgumentException(
                "Unsupported evidence image format.");
    }
}