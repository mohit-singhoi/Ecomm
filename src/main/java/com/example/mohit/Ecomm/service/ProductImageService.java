package com.example.mohit.Ecomm.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.ProductImage;
import com.example.mohit.Ecomm.repo.ProductImageRepository;

@Service
public class ProductImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final ProductImageRepository productImageRepository;
    private final Path uploadDirectory;

    public ProductImageService(
            ProductImageRepository productImageRepository,
            @Value("${app.upload.dir:uploads/products}")
            String uploadDirectory) {

        this.productImageRepository = productImageRepository;
        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not initialize product image storage.", e);
        }
    }

    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository
                .findByProduct_IdOrderByDisplayOrderAsc(productId);
    }

    @Transactional
    public ProductImage uploadImage(
            Product product,
            MultipartFile file,
            boolean primaryImage) throws IOException {

        validateImage(file);

        List<ProductImage> existingImages =
                getProductImages(product.getId());

        String filename = UUID.randomUUID()
                + getSafeExtension(file.getOriginalFilename());

        Path destination = uploadDirectory.resolve(filename).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IOException("Invalid image filename.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination,
                    StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl("/uploads/products/" + filename);
            image.setDisplayOrder(existingImages.size());

            boolean shouldBePrimary =
                    primaryImage || existingImages.isEmpty();

            if (shouldBePrimary) {
                existingImages.forEach(existing -> {
                    existing.setPrimaryImage(false);
                    productImageRepository.save(existing);
                });
            }

            image.setPrimaryImage(shouldBePrimary);

            return productImageRepository.save(image);

        } catch (RuntimeException e) {
            Files.deleteIfExists(destination);
            throw e;
        }
    }

    @Transactional
    public void setPrimaryImage(Long productId, Long imageId) {

        List<ProductImage> images = getProductImages(productId);

        ProductImage selected = images.stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Image does not belong to this product."));

        images.forEach(image -> {
            image.setPrimaryImage(
                    image.getId().equals(selected.getId()));
        });

        productImageRepository.saveAll(images);
    }

    private void validateImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select an image to upload.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Each image must be 5 MB or smaller.");
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG, and WEBP images are allowed.");
        }

        String extension =
                getSafeExtension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported image extension.");
        }

        // Check the actual file signature instead of trusting the filename
        byte[] header = new byte[12];

        try (InputStream inputStream = file.getInputStream()) {
            int bytesRead = inputStream.read(header);

            if (!isValidImageSignature(header, bytesRead)) {
                throw new IllegalArgumentException(
                        "The uploaded file is not a valid supported image.");
            }
        }
    }

    private boolean isValidImageSignature(
            byte[] header, int length) {

        if (length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF) {
            return true;
        }

        if (length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A) {
            return true;
        }

        return length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }

    private String getSafeExtension(String originalFilename) {

        if (originalFilename == null) {
            throw new IllegalArgumentException(
                    "Image filename is missing.");
        }

        String filename = Paths.get(originalFilename)
                .getFileName()
                .toString();

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0) {
            throw new IllegalArgumentException(
                    "Image file must have an extension.");
        }

        return filename.substring(dotIndex).toLowerCase();
    }
}