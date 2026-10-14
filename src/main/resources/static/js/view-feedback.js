document.addEventListener("DOMContentLoaded", function () {

    const responseField = document.getElementById("adminResponse");
    const preview = document.getElementById("responsePreviewText");
    const counter = document.getElementById("responseCounter");
    const form = document.getElementById("adminResponseForm");

    if (!responseField || !preview || !counter) {
        return;
    }

    const defaultResponse = `Dear Customer,

Thank you for sharing your feedback with ShopSphere. We appreciate you taking the time to help us improve our services. Our team has reviewed your concern and will work toward resolving it as soon as possible.

Best regards,
ShopSphere Customer Support`;

    // Populate the default response only for a new, empty response.
    if (responseField.value.trim() === "") {
        responseField.value = defaultResponse;
    }

    function updatePreview() {
        preview.textContent = responseField.value;
        counter.textContent = `${responseField.value.length} / 2000`;

        counter.style.color =
            responseField.value.length > 1900 ? "#dc2626" : "#64748b";
    }

    responseField.addEventListener("input", updatePreview);

    updatePreview();

    if (form) {
        form.addEventListener("submit", function (event) {

            if (responseField.value.trim().length < 10) {
                event.preventDefault();

                alert("Please enter a response containing at least 10 characters.");

                responseField.focus();
            }

        });
    }

});