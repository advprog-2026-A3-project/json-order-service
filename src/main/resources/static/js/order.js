document.addEventListener("DOMContentLoaded", function () {
    var deleteForms = document.querySelectorAll(".js-delete-form");
    deleteForms.forEach(function (form) {
        form.addEventListener("submit", function (event) {
            var product = form.getAttribute("data-product") || "order ini";
            var confirmed = window.confirm("Hapus " + product + " dari database?");
            if (!confirmed) {
                event.preventDefault();
            }
        });
    });

    var alerts = document.querySelectorAll(".alert[data-autohide='true']");
    alerts.forEach(function (alert) {
        window.setTimeout(function () {
            alert.style.display = "none";
        }, 3500);
    });
});

