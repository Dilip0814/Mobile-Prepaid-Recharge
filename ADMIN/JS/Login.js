const BASE_URL = "http://localhost:8083/admin"; // Backend URL

// Function to show error messages below input fields
function showError(fieldId, message) {
    const errorDiv = document.getElementById(fieldId);
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
}

// Function to hide error messages
function hideError(fieldId) {
    const errorDiv = document.getElementById(fieldId);
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
}

// Function to clear input fields
function clearInputFields(...fieldIds) {
    fieldIds.forEach(fieldId => {
        document.getElementById(fieldId).value = '';
    });
}

// Function to show success alerts
function showAlert(alertId, message) {
    const alertDiv = document.getElementById(alertId);
    if (alertDiv) {
        alertDiv.querySelector('span').textContent = message;
        alertDiv.style.display = 'block';
        setTimeout(() => {
            alertDiv.style.display = 'none';
        }, 2000);
    }
}

// Add event listeners to input fields to hide error messages when typing
document.getElementById('username').addEventListener('input', function() {
    hideError('usernameError');
});

document.getElementById('password').addEventListener('input', function() {
    hideError('passwordError');
});

document.getElementById('email').addEventListener('input', function() {
    hideError('emailError');
});

document.getElementById('newPassword').addEventListener('input', function() {
    hideError('newPasswordError');
});

document.getElementById('confirmPassword').addEventListener('input', function() {
    hideError('newPasswordError');
});

// Admin Login
document.getElementById('loginForm').addEventListener('submit', async function(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Hide previous error messages
    hideError('usernameError');
    hideError('passwordError');

    try {
        const response = await fetch(`${BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
        });

        const responseData = await response.text(); // Get the response message

        if (response.ok) {
            // Login successful
            localStorage.setItem('adminUsername', username); // Store username in local storage
            showAlert('successAlert', 'Login successful!');
            setTimeout(() => {
                window.location.href = "Home.html"; // Redirect to SPA
            }, 2000);
        } else {
            // Login failed
            showError('passwordError', responseData); // Display error message from the backend
            clearInputFields('password'); // Clear the password field
        }
    } catch (error) {
        console.error('Error during login:', error);
        showError('passwordError', 'Login failed. Please try again.');
        clearInputFields('password');
    }
});

// Forgot Password
document.getElementById('forgotPasswordLink').addEventListener('click', function(event) {
    event.preventDefault();
    document.getElementById('loginContainer').style.display = 'none';
    document.getElementById('forgotPasswordContainer').style.display = 'block';
});

// Back to Login from Forgot Password
document.getElementById('backToLoginLink').addEventListener('click', function(event) {
    event.preventDefault();
    document.getElementById('forgotPasswordContainer').style.display = 'none';
    document.getElementById('loginContainer').style.display = 'block';
});

// Back to Login from Reset Password
document.getElementById('backToLoginLink2').addEventListener('click', function(event) {
    event.preventDefault();
    document.getElementById('resetPasswordContainer').style.display = 'none';
    document.getElementById('loginContainer').style.display = 'block';
});

// Forgot Password Form Submission
document.getElementById('forgotPasswordForm').addEventListener('submit', async function(event) {
    event.preventDefault();
    const email = document.getElementById('email').value;

    // Hide previous error messages
    hideError('emailError');

    try {
        if (!email) {
            showError('emailError', 'Please enter a valid email.');
            return;
        }

        const response = await fetch(`${BASE_URL}/validate-email`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `email=${encodeURIComponent(email)}`,
        });

        const responseData = await response.text();

        if (response.ok) {
            document.getElementById('forgotPasswordContainer').style.display = 'none';
            document.getElementById('resetPasswordContainer').style.display = 'block';
        } else {
            showError('emailError', responseData);
            clearInputFields('email');
        }
    } catch (error) {
        console.error('Error during forgot password:', error);
        showError('emailError', 'Failed to process your request. Please try again.');
        clearInputFields('email');
    }
});

// Reset Password Form Submission
document.getElementById('resetPasswordForm').addEventListener('submit', async function(event) {
    event.preventDefault();
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const email = document.getElementById('email').value;

    hideError('newPasswordError');

    if (!newPassword || !confirmPassword) {
        showError('newPasswordError', 'Please fill in both password fields.');
        clearInputFields('newPassword', 'confirmPassword');
        return;
    }

    if (newPassword !== confirmPassword) {
        showError('newPasswordError', 'Passwords do not match.');
        clearInputFields('newPassword', 'confirmPassword');
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/reset-password`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `email=${encodeURIComponent(email)}&newPassword=${encodeURIComponent(newPassword)}&confirmPassword=${encodeURIComponent(confirmPassword)}`,
        });

        const responseData = await response.text();

        if (response.ok) {
            showAlert('passwordChangedAlert', 'Password reset successfully!');
            setTimeout(() => {
                document.getElementById('resetPasswordContainer').style.display = 'none';
                document.getElementById('loginContainer').style.display = 'block';
            }, 2000);
        } else {
            showError('newPasswordError', responseData);
            clearInputFields('newPassword', 'confirmPassword');
        }
    } catch (error) {
        console.error('Error during password reset:', error);
        showError('newPasswordError', 'Password reset failed. Please try again.');
        clearInputFields('newPassword', 'confirmPassword');
    }
});