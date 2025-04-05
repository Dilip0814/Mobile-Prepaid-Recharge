document.addEventListener("DOMContentLoaded", function() {
    // Load username display
    loadUsername();
    
    // Handle person icon click
    const personIcon = document.querySelector(".person-icon");
    if (personIcon) {
        personIcon.addEventListener("click", function(e) {
            const username = localStorage.getItem("username");
            if (username) {
                // If logged in, redirect to profile page
                e.preventDefault();
                window.location.href = "profile_page.html";
            } else {
                // If not logged in, redirect to login page with fromNavbar flag
                e.preventDefault();
                window.location.href = "index.html?fromNavbar=true";
            }
        });
    }
});

function loadUsername() {
    const username = localStorage.getItem("username");
    const usernameDisplay = document.getElementById("usernameDisplay");
    const usernameDisplayMobile = document.getElementById("usernameDisplayMobile");
    
    if (username) {
        if (usernameDisplay) {
            usernameDisplay.innerHTML = username;
            usernameDisplay.style.cursor = "pointer";
        }
        if (usernameDisplayMobile) {
            usernameDisplayMobile.innerHTML = username;
            usernameDisplayMobile.style.cursor = "pointer";
        }
    } else {
        if (usernameDisplay) {
            usernameDisplay.innerHTML = '<i class="bi bi-person-circle"></i>';
        }
        if (usernameDisplayMobile) {
            usernameDisplayMobile.innerHTML = '<i class="bi bi-person-circle"></i>';
        }
    }
}