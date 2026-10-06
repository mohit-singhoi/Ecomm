document.addEventListener("DOMContentLoaded", function () {

    const popups = [
        document.getElementById("authPopup"),
        document.getElementById("authErrorPopup")
    ];

    popups.forEach(function (popup) {

        if (popup) {

            // Keep popup visible for 2 seconds
            setTimeout(function () {

                popup.style.animation =
                    "authPopupOut 0.25s ease forwards";

                // Remove after fade-out
                setTimeout(function () {
                    popup.remove();
                }, 250);

            }, 2000);
        }
    });

});