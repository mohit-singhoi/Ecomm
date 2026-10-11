package com.example.mohit.Ecomm.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    // Show all users with search and role filtering
    @GetMapping
    public String showUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check administrator login
        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login as administrator."
            );

            return "redirect:/adminlogin";
        }

        if (!"ROLE_ADMIN".equals(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required."
            );

            return "redirect:/dashboard";
        }

        // Get all users
        List<User> allUsers = userRepository.findAll();

        // Calculate user statistics
        long totalUsers = allUsers.size();

        long customerCount = allUsers.stream()
                .filter(user ->
                        "ROLE_USER".equals(user.getRole()))
                .count();

        long adminCount = allUsers.stream()
                .filter(user ->
                        "ROLE_ADMIN".equals(user.getRole()))
                .count();

        // Apply search and role filtering
        List<User> users = allUsers.stream()
                .filter(user -> {
                    if (search == null || search.isBlank()) {
                        return true;
                    }

                    String keyword =
                            search.trim().toLowerCase(Locale.ROOT);

                    boolean matchesName =
                            user.getName() != null
                            && user.getName()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(keyword);

                    boolean matchesEmail =
                            user.getEmail() != null
                            && user.getEmail()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(keyword);

                    return matchesName || matchesEmail;
                })
                .filter(user ->
                        role == null
                        || role.isBlank()
                        || role.equals(user.getRole()))
                .toList();

        // Send data to Thymeleaf
        model.addAttribute("admin", loggedInUser);
        model.addAttribute("users", users);

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("adminCount", adminCount);

        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("activePage", "users");

        return "admin/users";
    }

    // View individual user details
    @GetMapping("/view/{id}")
    public String viewUser(
            @PathVariable Long id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check administrator login
        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login as administrator."
            );

            return "redirect:/adminlogin";
        }

        if (!"ROLE_ADMIN".equals(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required."
            );

            return "redirect:/dashboard";
        }

        // Find user by ID
        User selectedUser =
                userRepository.findById(id).orElse(null);

        if (selectedUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "User not found!"
            );

            return "redirect:/admin/users";
        }

        // Send data to Thymeleaf
        model.addAttribute("admin", loggedInUser);
        model.addAttribute("user", selectedUser);
        model.addAttribute("activePage", "users");

        return "admin/user-details";
    }
    
 // Show edit-user form
    @GetMapping("/edit/{id}")
    public String showEditUserForm(
            @PathVariable Long id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Check administrator login
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login as administrator.");

            return "redirect:/adminlogin";
        }

        if (!"ROLE_ADMIN".equals(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required.");

            return "redirect:/dashboard";
        }

        // Find selected user
        User selectedUser =
                userRepository.findById(id).orElse(null);

        if (selectedUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "User not found!");

            return "redirect:/admin/users";
        }

        model.addAttribute("admin", loggedInUser);
        model.addAttribute("user", selectedUser);
        model.addAttribute("activePage", "users");

        return "admin/edit-user";
    }
    
 // Update existing user
    @PostMapping("/update/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute User user,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Check administrator login
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login as administrator.");

            return "redirect:/adminlogin";
        }

        if (!"ROLE_ADMIN".equals(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator permission required.");

            return "redirect:/dashboard";
        }

        // Find existing user
        User existingUser =
                userRepository.findById(id).orElse(null);

        if (existingUser == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "User not found!");

            return "redirect:/admin/users";
        }

        // Update only the permitted fields
        String name = user.getName() == null
                ? ""
                : user.getName().trim();

        String email = user.getEmail() == null
                ? ""
                : user.getEmail().trim();

        if (name.isBlank() || email.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Name and email are required.");

            return "redirect:/admin/users/edit/" + id;
        }

        // Prevent duplicate email addresses
        User userWithSameEmail =
                userRepository.findByEmail(email).orElse(null);

        if (userWithSameEmail != null
                && !userWithSameEmail.getId().equals(id)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This email address is already registered.");

            return "redirect:/admin/users/edit/" + id;
        }

        try {
            existingUser.setName(name);
            existingUser.setEmail(email);

            // Do not overwrite the password or role through this form.
            userRepository.save(existingUser);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "User details updated successfully!");

            return "redirect:/admin/users/view/" + id;

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to update user details.");

            return "redirect:/admin/users/edit/" + id;
        }
    }
    
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check admin session
        User admin = (User) session.getAttribute("loggedInUser");

        if (admin == null || !"ROLE_ADMIN".equals(admin.getRole())) {
            return "redirect:/adminlogin";
        }

        // Find the selected user
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "User not found!"
            );

            return "redirect:/admin/users";
        }

        // Prevent changing your own admin account status
        if (admin.getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You cannot deactivate your own admin account!"
            );

            return "redirect:/admin/users";
        }

        // Toggle account status
        user.setActive(!user.isActive());

        userRepository.save(user);

        // Display success message
        String message = user.isActive()
                ? "User activated successfully!"
                : "User deactivated successfully!";

        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/admin/users";
    }
}