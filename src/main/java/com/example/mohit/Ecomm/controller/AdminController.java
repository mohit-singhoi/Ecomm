package com.example.mohit.Ecomm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.OrderRepository;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ADMIN LOGIN PAGE
    @GetMapping("/adminlogin")
    public String adminLogin() {
        return "adminlogin";
    }

    // ADMIN DASHBOARD
    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

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

        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();

        Double totalSales = orderRepository.getTotalSales();

        if (totalSales == null) {
            totalSales = 0.0;
        }

        model.addAttribute("admin", loggedInUser);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }
    
    
 // =====================================================
 // ADMIN USER MANAGEMENT
 // =====================================================
 @GetMapping("/admin/users")
 public String manageUsers(
         @RequestParam(required = false) String search,
         @RequestParam(required = false) String role,
         HttpSession session,
         Model model,
         RedirectAttributes redirectAttributes) {

     // Check whether the user is logged in
     User loggedInUser =
             (User) session.getAttribute("loggedInUser");

     if (loggedInUser == null) {
         redirectAttributes.addFlashAttribute(
                 "errorMessage",
                 "Please login as administrator."
         );

         return "redirect:/adminlogin";
     }

     // Check administrator permission
     if (!"ROLE_ADMIN".equals(loggedInUser.getRole())) {
         redirectAttributes.addFlashAttribute(
                 "errorMessage",
                 "Administrator permission required."
         );

         return "redirect:/dashboard";
     }

     // Fetch all registered users
     List<User> allUsers = userRepository.findAll();

     // Count customers and administrators
     long customerCount = allUsers.stream()
             .filter(user -> "ROLE_USER".equals(user.getRole()))
             .count();

     long adminCount = allUsers.stream()
             .filter(user -> "ROLE_ADMIN".equals(user.getRole()))
             .count();

     // Search users by name or email and filter by role
     List<User> filteredUsers = allUsers.stream()
             .filter(user -> {
                 if (search == null || search.trim().isEmpty()) {
                     return true;
                 }

                 String keyword = search.trim().toLowerCase();

                 String name = user.getName() == null
                         ? ""
                         : user.getName().toLowerCase();

                 String email = user.getEmail() == null
                         ? ""
                         : user.getEmail().toLowerCase();

                 return name.contains(keyword)
                         || email.contains(keyword);
             })
             .filter(user ->
                     role == null
                     || role.trim().isEmpty()
                     || role.equals(user.getRole())
             )
             .collect(Collectors.toList());

     // Send data to the Thymeleaf page
     model.addAttribute("admin", loggedInUser);
     model.addAttribute("users", filteredUsers);

     // Statistics use all registered users, not filtered results
     model.addAttribute("totalUsers", allUsers.size());
     model.addAttribute("customerCount", customerCount);
     model.addAttribute("adminCount", adminCount);

     // Preserve filter values in the form
     model.addAttribute("search", search);
     model.addAttribute("role", role);

     model.addAttribute("activePage", "users");

     return "admin/users";
 }
    
    
}