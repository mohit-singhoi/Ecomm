package com.example.mohit.Ecomm.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

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
                        .filter(product ->
                                product.getCategory() != null
                                && product.getCategory()
                                        .equalsIgnoreCase(category))
                        .toList();
            }

        } else if (category != null && !category.isBlank()) {

            products = productService.getProductsByCategory(category);

        } else {

            products = productService.getAllProducts();
        }

        // Product list
        model.addAttribute("products", products);

        // Preserve search and category selections
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("activePage", "products");

        // Total number of products in the database
        model.addAttribute(
                "totalProducts",
                productService.getAllProducts().size()
        );

        // Number of available categories in the displayed products
        long categoryCount = products.stream()
                .map(Product::getCategory)
                .filter(categoryName ->
                        categoryName != null && !categoryName.isBlank())
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

    // Save new product
    @PostMapping("/save")
    public String saveProduct(
            @ModelAttribute Product product,
            RedirectAttributes redirectAttributes) {

        try {

            product.setId(null);

            productService.saveProduct(product);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product added successfully!"
            );

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to add the product. Please check the product details."
            );

            return "redirect:/admin/products/add";
        }

        return "redirect:/admin/products";
    }

    // Show edit-product form
    @GetMapping("/edit/{id}")
    public String showEditProductForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id);

        if (product == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found!"
            );

            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);

        return "admin/edit-product";
    }

 // View product details
    @GetMapping("/view/{id}")
    public String viewProduct(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id);

        if (product == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found!"
            );

            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("activePage", "products");

        return "admin/product-details";
    }
    
    
    // Update existing product
    @PostMapping("/update/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            RedirectAttributes redirectAttributes) {

        Product existingProduct = productService.getProductById(id);

        if (existingProduct == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found!"
            );

            return "redirect:/admin/products";
        }

        try {

            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setImageUrl(product.getImageUrl());
            existingProduct.setCategory(product.getCategory());

            productService.saveProduct(existingProduct);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product updated successfully!"
            );

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to update the product. Please check the product details."
            );
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id);

        if (product == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found!"
            );

            return "redirect:/admin/products";
        }

        try {
            // Check whether the product is used in an existing order
            if (productService.isProductUsedInOrders(id)) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "This product cannot be deleted because it is associated with existing orders."
                );

                return "redirect:/admin/products";
            }

            // Delete the product if it is not used in any order
            productService.deleteProduct(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product deleted successfully!"
            );

        } catch (Exception exception) {

            exception.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to delete this product. Please check the application console."
            );
        }

        return "redirect:/admin/products";
    }
}