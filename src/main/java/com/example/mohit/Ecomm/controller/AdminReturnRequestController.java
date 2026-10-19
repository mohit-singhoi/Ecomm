package com.example.mohit.Ecomm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.ReturnRequest;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.ReturnEvidenceStorageService;
import com.example.mohit.Ecomm.service.ReturnRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/returns")
public class AdminReturnRequestController {

    @Autowired
    private ReturnRequestService returnRequestService;

    @Autowired
    private ReturnEvidenceStorageService evidenceStorageService;


    // =====================================================
    // CHECK ADMINISTRATOR ACCESS
    // =====================================================

    private boolean isAdmin(HttpSession session) {

        User admin =
                (User) session.getAttribute("loggedInUser");

        return admin != null
                && "ROLE_ADMIN".equals(admin.getRole());
    }


    // =====================================================
    // DISPLAY ALL RETURN REQUESTS
    // URL: GET /admin/returns
    // =====================================================

    @GetMapping
    public String listReturnRequests(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator.");

            return "redirect:/adminlogin";
        }

        try {

            model.addAttribute(
                    "returnRequests",
                    returnRequestService
                            .getAllReturnRequestsForAdmin());

            model.addAttribute(
                    "activePage",
                    "returns");

            return "admin/return-requests";

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to load return requests.");

            return "redirect:/admin/dashboard";
        }
    }


    // =====================================================
    // DISPLAY INDIVIDUAL RETURN REQUEST
    // URL: GET /admin/returns/view/{id}
    // =====================================================

    @GetMapping("/view/{id}")
    public String viewReturnRequest(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator.");

            return "redirect:/adminlogin";
        }

        try {

            ReturnRequest request =
                    returnRequestService
                            .getReturnRequestForAdmin(id);

            model.addAttribute(
                    "returnRequest",
                    request);

            model.addAttribute(
                    "activePage",
                    "returns");

            return "admin/return-request-details";

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());

            return "redirect:/admin/returns";
        }
    }


    // =====================================================
    // APPROVE OR REJECT RETURN REQUEST
    // URL: POST /admin/returns/review/{id}
    // =====================================================

    @PostMapping("/review/{id}")
    public String reviewReturnRequest(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false)
            String adminRemarks,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isAdmin(session)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in as an administrator.");

            return "redirect:/adminlogin";
        }

        try {

            ReturnRequest request =
                    returnRequestService.reviewReturnRequest(
                            id,
                            decision,
                            adminRemarks);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Return request #"
                            + request.getId()
                            + " updated successfully. Status: "
                            + request.getStatus());

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage());
        }

        return "redirect:/admin/returns/view/" + id;
    }


    // =====================================================
    // VIEW CUSTOMER'S UPLOADED EVIDENCE IMAGE
    // URL: GET /admin/returns/evidence/{requestId}
    // =====================================================

    @GetMapping("/evidence/{requestId}")
    public ResponseEntity<Resource> viewEvidence(
            @PathVariable Long requestId,
            HttpSession session) {

        if (!isAdmin(session)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator access is required.");
        }

        try {

            // Retrieve the request and its associated details
            ReturnRequest request =
                    returnRequestService
                            .getReturnRequestForAdmin(requestId);

            String filename =
                    request.getEvidenceImagePath();

            if (filename == null || filename.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No evidence image was uploaded.");
            }

            // Load the image from private storage
            Resource resource =
                    evidenceStorageService
                            .loadAsResource(filename);

            String contentType =
                    evidenceStorageService
                            .getContentType(filename);

            MediaType mediaType =
                    MediaType.parseMediaType(contentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"return-evidence\"")
                    .header(
                            "X-Content-Type-Options",
                            "nosniff")
                    .body(resource);

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (RuntimeException ex) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Return evidence was not found.");
        }
    }
}