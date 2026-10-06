document.addEventListener("DOMContentLoaded", function () {

    const profileButton =
        document.getElementById("userProfileBtn");

    const profileMenu =
        document.querySelector(".user-profile-menu");

    if (!profileButton || !profileMenu) {
        return;
    }


    /* ==========================================
       CLICK PROFILE BUTTON
       ========================================== */

    profileButton.addEventListener("click", function (event) {

        event.stopPropagation();

        const isOpen =
            profileMenu.classList.contains("show");

        if (isOpen) {

            profileMenu.classList.remove("show");

            profileButton.setAttribute(
                "aria-expanded",
                "false"
            );

        } else {

            profileMenu.classList.add("show");

            profileButton.setAttribute(
                "aria-expanded",
                "true"
            );
        }
    });


    /* ==========================================
       PREVENT DROPDOWN CLICK FROM CLOSING IT
       ========================================== */

    profileMenu.addEventListener("click", function (event) {
        event.stopPropagation();
    });


    /* ==========================================
       CLICK OUTSIDE
       ========================================== */

    document.addEventListener("click", function () {

        profileMenu.classList.remove("show");

        profileButton.setAttribute(
            "aria-expanded",
            "false"
        );
    });

});