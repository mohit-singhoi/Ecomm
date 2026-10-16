package com.example.mohit.Ecomm.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.ProductImage;
import com.example.mohit.Ecomm.service.ProductService;
import com.example.mohit.Ecomm.service.ProductImageService;
import com.example.mohit.Ecomm.repo.ProductImageRepository;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductImageRepository productImageRepository;

    // Show all products with search and category filtering
    @GetMapping
    public String showProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model) {

        List<Product> products;

        if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search.trim());

            if (category != null && !category.isBlank()) {
                products = products.stream()
                        .filter(product -> product.getCategory() != null
                                && product.getCategory()
                                        .equalsIgnoreCase(category))
                        .toList();
            }

        } else if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category);

        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("activePage", "products");

        model.addAttribute(
                "totalProducts",
                productService.getAllProducts().size());

        long categoryCount = products.stream()
                .map(Product::getCategory)
                .filter(name -> name != null && !name.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .count();

        model.addAttribute("categoryCount", categoryCount);

        return "admin/products";
    }

    // Show add-product form
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/add-product";
    }

    // Save new product and upload its gallery images
    @PostMapping("/save")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam(value = "images", required = false)
            MultipartFile[] images,
            @RequestParam(value = "primaryImageIndex", required = false)
            Integer primaryImageIndex,
            RedirectAttributes redirectAttributes) {

        try {
            product.setId(null);

            // Save first so the product has a database ID.
            Product savedProduct = productService.saveProduct(product);

            uploadImages(savedProduct, images, primaryImageIndex);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product added successfully!");

        } catch (Exception exception) {
            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to add the product. Please check the product details and images.");

            return "redirect:/admin/products/add";
        }

        return "redirect:/admin/products";
    }

    // Show edit-product form and existing gallery
    @GetMapping("/edit/{id}")
    public String showEditProductForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id);

        if (product == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Product not found!");

            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute(
                "productImages",
                productImageService.getProductImages(id));

        return "admin/edit-product";
    }

    // View product details in the existing admin template
    @GetMapping("/view/{id}")
    public String viewProduct(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id);

        if (product == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Product not found!");

            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute(
                "productImages",
                productImageService.getProductImages(id));
        model.addAttribute("activePage", "products");

        return "admin/product-details";
    }

    // Update product and add new gallery images
    @PostMapping("/update/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            @RequestParam(value = "images", required = false)
            MultipartFile[] images,
            @RequestParam(value = "primaryImageIndex", required = false)
            Integer primaryImageIndex,
            RedirectAttributes redirectAttributes) {

        Product existingProduct = productService.getProductById(id);

        if (existingProduct == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Product not found!");

            return "redirect:/admin/products";
        }

        try {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setImageUrl(product.getImageUrl());
            existingProduct.setCategory(product.getCategory());

            Product savedProduct =
                    productService.saveProduct(existingProduct);

            uploadImages(savedProduct, images, primaryImageIndex);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product updated successfully!");

        } catch (Exception exception) {
            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to update the product. Please check the product details and images.");
        }

        return "redirect:/admin/products";
    }

    /*
     * Uploads selected images.
     * primaryImageIndex is zero-based and refers to the uploaded files
     * in the order received by the server.
     */
    private void uploadImages(
            Product product,
            MultipartFile[] images,
            Integer primaryImageIndex) throws IOException {

        if (images == null || images.length == 0) {
            return;
        }

        int selectedIndex = primaryImageIndex == null
                ? -1
                : primaryImageIndex;

        if (selectedIndex < -1 || selectedIndex >= images.length) {
            throw new IllegalArgumentException(
                    "Invalid primary image selection.");
        }

        boolean hasExistingImages =
                !productImageService.getProductImages(product.getId())
                        .isEmpty();

        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];

            // Ignore empty file fields.
            if (image == null || image.isEmpty()) {
                continue;
            }

            boolean makePrimary;

            if (selectedIndex >= 0) {
                makePrimary = i == selectedIndex;
            } else {
                makePrimary = !hasExistingImages && i == 0;
            }

            ProductImage savedImage =
                    productImageService.uploadImage(
                            product, image, makePrimary);

            if (savedImage.isPrimaryImage()) {
                // Keep the old imageUrl field compatible with existing pages.
                product.setImageUrl(savedImage.getImageUrl());
                productService.saveProduct(product);
            }

            hasExistingImages = true;
        }
    }

    // Set an existing gallery image as primary
    @PostMapping("/{productId}/images/{imageId}/primary")
    public String setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {

        try {
            productImageService.setPrimaryImage(productId, imageId);

            Product product = productService.getProductById(productId);
            List<ProductImage> images =
                    productImageService.getProductImages(productId);

            if (product != null) {
                images.stream()
                        .filter(ProductImage::isPrimaryImage)
                        .findFirst()
                        .ifPresent(primary -> {
                            product.setImageUrl(primary.getImageUrl());
                            productService.saveProduct(product);
                        });
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Primary image updated successfully!");

        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Unable to update the primary image.");
        }

        return "redirect:/admin/products/edit/" + productId;
    }

    // Remove an image from a product gallery
    @PostMapping("/{productId}/images/{imageId}/delete")
    public String deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {

        try {
            Product product = productService.getProductById(productId);

            if (product == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage", "Product not found!");

                return "redirect:/admin/products";
            }

            ProductImage image = productImageRepository.findById(imageId)
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .orElse(null);

            if (image == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Image not found for this product.");

                return "redirect:/admin/products/edit/" + productId;
            }

            boolean wasPrimary = image.isPrimaryImage();

            // Delete the database record. File cleanup will be integrated
            // into ProductImageService in the next step.
            productImageRepository.delete(image);

            if (wasPrimary) {
                List<ProductImage> remainingImages =
                        productImageService.getProductImages(productId);

                if (!remainingImages.isEmpty()) {
                    ProductImage newPrimary = remainingImages.get(0);
                    newPrimary.setPrimaryImage(true);
                    productImageRepository.save(newPrimary);
                    product.setImageUrl(newPrimary.getImageUrl());
                }

                productService.saveProduct(product);
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Product image removed successfully!");

        } catch (Exception exception) {
            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Unable to remove the product image.");
        }

        return "redirect:/admin/products/edit/" + productId;
    }

    // Delete product while preserving existing order-history safeguards
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            String result = productService.deleteProduct(id);

            switch (result) {
                case "DELETED" -> redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Product permanently deleted successfully!");

                case "DEACTIVATED" -> redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Product removed from the store. Existing order history has been preserved.");

                case "NOT_FOUND" -> redirectAttributes.addFlashAttribute(
                        "errorMessage", "Product not found!");

                default -> redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Unable to process the product deletion.");
            }

        } catch (Exception exception) {
            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product removal failed. Please check the application console.");
        }

        return "redirect:/admin/products";
    }
}