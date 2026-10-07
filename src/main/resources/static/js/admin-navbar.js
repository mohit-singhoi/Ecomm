(function () {

    function initializeAdminNavbar() {

        const menuToggle = document.getElementById("adminMenuToggle");
        const navigation = document.getElementById("adminNavigation");

        console.log("ShopSphere Admin Navbar JS started");

        if (!menuToggle) {
            console.error("adminMenuToggle was NOT found");
            return;
        }

        if (!navigation) {
            console.error("adminNavigation was NOT found");
            return;
        }

        console.log("Admin navbar found successfully");


        // =========================================
        // MENU BUTTON
        // =========================================

        menuToggle.addEventListener("click", function (event) {

            event.preventDefault();
            event.stopPropagation();

            navigation.classList.toggle("show");

            const menuIsOpen =
                navigation.classList.contains("show");


            menuToggle.setAttribute(
                "aria-expanded",
                menuIsOpen ? "true" : "false"
            );


            if (menuIsOpen) {

                menuToggle.innerHTML =
                    '<i class="fa-solid fa-xmark"></i>';

                console.log("Admin menu OPEN");

            } else {

                menuToggle.innerHTML =
                    '<i class="fa-solid fa-bars"></i>';

                console.log("Admin menu CLOSED");
            }

        });


        // =========================================
        // NAVIGATION CLICK
        // =========================================

        navigation.addEventListener("click", function (event) {

            event.stopPropagation();

        });


        // =========================================
        // NAVIGATION LINKS
        // =========================================

        const links =
            navigation.querySelectorAll("a");


        links.forEach(function (link) {

            link.addEventListener("click", function () {

                navigation.classList.remove("show");

                menuToggle.setAttribute(
                    "aria-expanded",
                    "false"
                );

                menuToggle.innerHTML =
                    '<i class="fa-solid fa-bars"></i>';

            });

        });


        // =========================================
        // CLICK OUTSIDE
        // =========================================

        document.addEventListener("click", function (event) {

            if (!event.target.closest(".admin-navbar")) {

                navigation.classList.remove("show");

                menuToggle.setAttribute(
                    "aria-expanded",
                    "false"
                );

                menuToggle.innerHTML =
                    '<i class="fa-solid fa-bars"></i>';
            }

        });


        // =========================================
        // WINDOW RESIZE
        // =========================================

        window.addEventListener("resize", function () {

            if (window.innerWidth > 900) {

                navigation.classList.remove("show");

                menuToggle.setAttribute(
                    "aria-expanded",
                    "false"
                );

                menuToggle.innerHTML =
                    '<i class="fa-solid fa-bars"></i>';
            }

        });

    }


    // =============================================
    // INITIALIZE
    // =============================================

    if (document.readyState === "loading") {

        document.addEventListener(
            "DOMContentLoaded",
            initializeAdminNavbar
        );

    } else {

        initializeAdminNavbar();

    }

})();