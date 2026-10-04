document.addEventListener("DOMContentLoaded", function () {

    const quantityInput = document.getElementById("quantity");
    const cartQuantity = document.getElementById("cartQuantity");
    const buyQuantity = document.getElementById("buyQuantity");

    if (!quantityInput || !cartQuantity || !buyQuantity) {
        return;
    }

    quantityInput.addEventListener("input", function () {

        let quantity = parseInt(quantityInput.value);

        if (isNaN(quantity) || quantity < 1) {
            quantity = 1;
            quantityInput.value = 1;
        }

        cartQuantity.value = quantity;
        buyQuantity.value = quantity;
    });

});