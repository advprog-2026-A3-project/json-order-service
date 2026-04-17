document.addEventListener("DOMContentLoaded", function () {
    var cancelForms = document.querySelectorAll(".js-cancel-form");
    cancelForms.forEach(function (form) {
        form.addEventListener("submit", function (event) {
            var product = form.getAttribute("data-product") || "order ini";
            var confirmed = window.confirm("Batalkan " + product + "?");
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
