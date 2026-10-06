document.addEventListener("DOMContentLoaded", function () {

    const popup = document.getElementById("successPopup");

    if (popup) {

        setTimeout(function () {

            popup.style.animation =
                "popupFadeOut 0.3s ease forwards";

            setTimeout(function () {
                popup.remove();
            }, 300);

        }, 3000);

    }

});