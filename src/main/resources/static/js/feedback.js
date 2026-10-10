// =========================================================
// ShopSphere - Feedback Page JavaScript
// File: feedback.js
// =========================================================

document.addEventListener("DOMContentLoaded", function () {

    // =====================================================
    // 1. Automatically hide success message after 3 seconds
    // =====================================================

    const successAlert = document.querySelector(".alert-success");

    if (successAlert) {
        setTimeout(function () {

            successAlert.style.transition = "opacity 0.5s ease";
            successAlert.style.opacity = "0";

            setTimeout(function () {
                successAlert.remove();
            }, 500);

        }, 3000);
    }


    // =====================================================
    // 2. Message character counter
    // =====================================================

    const messageField = document.getElementById("message");
    const characterCounter = document.getElementById("characterCounter");

    function updateCharacterCounter() {

        if (!messageField || !characterCounter) {
            return;
        }

        const length = messageField.value.length;

        characterCounter.textContent = length + " / 2000";

        characterCounter.classList.toggle(
            "counter-warning",
            length > 1800
        );
    }

    if (messageField && characterCounter) {
        messageField.addEventListener("input", updateCharacterCounter);
        updateCharacterCounter();
    }


    // =====================================================
    // 3. Star rating caption
    // =====================================================

    const ratingCaption = document.getElementById("ratingCaption");
    const ratingInputs = document.querySelectorAll(".star-rating input");

    const ratingLabels = {
        "1": "Poor",
        "2": "Fair",
        "3": "Good",
        "4": "Very Good",
        "5": "Excellent"
    };

    ratingInputs.forEach(function (input) {

        input.addEventListener("change", function () {

            if (ratingCaption) {
                ratingCaption.textContent = ratingLabels[this.value];
            }

        });

    });


    // =====================================================
    // 4. Reset rating caption and character counter
    // =====================================================

    const feedbackForm = document.getElementById("feedbackForm");

    if (feedbackForm) {

        feedbackForm.addEventListener("reset", function () {

            setTimeout(function () {

                if (ratingCaption) {
                    ratingCaption.textContent = "Select your rating";
                }

                updateCharacterCounter();

            }, 0);

        });


        // =================================================
        // 5. Prevent accidental double submission
        // =================================================

        const submitButton = document.getElementById("submitButton");

        feedbackForm.addEventListener("submit", function (event) {

            if (!feedbackForm.checkValidity()) {

                event.preventDefault();
                feedbackForm.reportValidity();

                return;
            }

            if (submitButton) {

                submitButton.disabled = true;

                submitButton.innerHTML =
                    '<i class="fa-solid fa-spinner fa-spin me-2"></i>Submitting...';

            }

        });

    }

});