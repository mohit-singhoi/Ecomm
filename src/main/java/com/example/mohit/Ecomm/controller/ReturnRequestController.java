package com.example.mohit.Ecomm.controller;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mohit.Ecomm.model.OrderItem;
import com.example.mohit.Ecomm.model.ReturnRequest;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.service.ReturnEvidenceStorageService;
import com.example.mohit.Ecomm.service.ReturnRequestService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my-orders/return")
public class ReturnRequestController {

    @Autowired
    private ReturnRequestService returnRequestService;

    @Autowired
    private ReturnEvidenceStorageService evidenceStorageService;

    // =====================================================
    // ALLOWED RETURN REASONS
    // =====================================================

    private final List<String> returnReasons = List.of(
            "Damaged or defective product",
            "Wrong product delivered",
            "Product differs from description",
            "Size or fit issue",
            "Other"
    );

    // =====================================================
    // DISPLAY RETURN REQUEST FORM
    // =====================================================

    @GetMapping("/{orderItemId}")
    public String showReturnForm(
            @PathVariable Long orderItemId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Verify customer login
        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/my-orders/return/" + orderItemId
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in to request a return."
            );

            return "redirect:/userlogin";
        }

        try {

            // Validate ownership, delivery status and return eligibility
            OrderItem item =
                    returnRequestService.getEligibleOrderItem(
                            orderItemId,
                            loggedInUser.getId()
                    );

            Date deliveredAt =
                    item.getOrder().getDeliveredAt();

            // Calculate the proposed seven-day return deadline
            Date returnDeadline = Date.from(
                    deliveredAt.toInstant()
                            .plus(7, ChronoUnit.DAYS)
            );

            model.addAttribute("returnItem", item);

            model.addAttribute(
                    "returnReasons",
                    returnReasons
            );

            model.addAttribute(
                    "returnDeadline",
                    returnDeadline
            );

            return "return-request";

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

            return "redirect:/my-orders";
        }
    }

    // =====================================================
    // SUBMIT RETURN REQUEST WITH OPTIONAL IMAGE
    // =====================================================

    @PostMapping("/{orderItemId}")
    public String submitReturnRequest(
            @PathVariable Long orderItemId,

            @RequestParam int returnQuantity,

            @RequestParam String reason,

            @RequestParam(required = false)
            String description,

            @RequestParam(
                    value = "evidenceImage",
                    required = false
            )
            MultipartFile evidenceImage,

            HttpSession session,

            RedirectAttributes redirectAttributes) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        // Verify customer login
        if (loggedInUser == null) {

            session.setAttribute(
                    "redirectAfterLogin",
                    "/my-orders/return/" + orderItemId
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please log in to request a return."
            );

            return "redirect:/userlogin";
        }

        try {

            // Validate the request and store the optional evidence image
            ReturnRequest request =
                    returnRequestService.createReturnRequest(
                            orderItemId,
                            loggedInUser.getId(),
                            returnQuantity,
                            reason,
                            description,
                            evidenceImage
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Return request #" + request.getId()
                            + " submitted successfully. "
                            + "Your request is awaiting review."
            );

        } catch (RuntimeException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/my-orders";
    }

    // =====================================================
    // VIEW RETURN EVIDENCE IMAGE
    // Only the request owner can access the image
    // =====================================================

    @GetMapping("/evidence/{requestId}")
    public ResponseEntity<Resource> viewEvidence(
            @PathVariable Long requestId,
            HttpSession session) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in to view this image."
            );
        }

        try {

            // Verify that the return request belongs to this customer
            ReturnRequest request =
                    returnRequestService.getMyReturnRequest(
                            requestId,
                            loggedInUser.getId()
                    );

            String filename =
                    request.getEvidenceImagePath();

            if (filename == null || filename.isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No evidence image was uploaded."
                );
            }

            // Load the image from private storage
            Resource resource =
                    evidenceStorageService.loadAsResource(filename);

            String contentType =
                    evidenceStorageService.getContentType(filename);

            MediaType mediaType =
                    MediaType.parseMediaType(contentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"return-evidence\""
                    )
                    .header(
                            "X-Content-Type-Options",
                            "nosniff"
                    )
                    .body(resource);

        } catch (ResponseStatusException ex) {

            throw ex;

        } catch (RuntimeException ex) {

            // Avoid exposing another customer's request or file
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Return evidence was not found."
            );
        }
    }
}