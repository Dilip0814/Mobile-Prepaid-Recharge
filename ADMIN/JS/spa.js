const BASE_URL = "http://localhost:8083/admin"; // Backend API base URL

// Fetch Functions
async function fetchUsers() {
    try {
        const response = await fetch(`${BASE_URL}/users`);
        return await response.json();
    } catch (error) {
        console.error("Error fetching users:", error);
        return [];
    }
}

async function fetchPlans() {
    try {
        const response = await fetch(`${BASE_URL}/plans`);
        return await response.json();
    } catch (error) {
        console.error("Error fetching plans:", error);
        return [];
    }
}

async function fetchTransactions() {
    try {
        const response = await fetch(`${BASE_URL}/transactions`);
        const transactions = await response.json();
        const plans = await fetchPlans();
        return transactions.map(t => ({ ...t, price: plans.find(p => p.planId === t.planId)?.price || 0 }));
    } catch (error) {
        console.error("Error fetching transactions:", error);
        return [];
    }
}

async function fetchCategories() {
    try {
        const response = await fetch(`${BASE_URL}/categories`);
        return await response.json();
    } catch (error) {
        console.error("Error fetching categories:", error);
        return [];
    }
}

async function fetchFeedback() {
    try {
        const response = await fetch(`${BASE_URL}/support`);
        if (!response.ok) throw new Error("Failed to fetch feedback");
        return await response.json();
    } catch (error) {
        console.error("Error fetching feedback:", error);
        return [];
    }
}

// Utility Functions
function calculateDaysToExpiry(transactionDate, expiryDate) {
    const today = new Date();
    const transaction = new Date(transactionDate);
    const expiry = new Date(expiryDate);
    const totalValidity = Math.ceil((expiry - transaction) / (1000 * 60 * 60 * 24));
    const daysPassed = Math.ceil((today - transaction) / (1000 * 60 * 60 * 24));
    return totalValidity - daysPassed >= 0 ? totalValidity - daysPassed : 0;
}

function formatIndianDate(dateString) {
    return new Date(dateString).toLocaleDateString('en-IN');
}

// Function to set admin username in navbar
function setAdminUsername() {
    const username = localStorage.getItem('adminUsername') || 'Admin User'; // Fallback if not set
    document.getElementById('adminUsername').textContent = username;
}

// Dashboard Functions
async function updateDashboard() {
    const users = await fetchUsers();
    const transactions = await fetchTransactions();

    const totalUsers = users.length;
    const activeUsers = users.filter(u => u.status.toLowerCase() === "active").length;
    const today = new Date().toISOString().split('T')[0];
    const expiryIn3Days = transactions.filter(t => calculateDaysToExpiry(t.transactionDate, t.expiryDate) <= 3).length;
    const revenueToday = transactions.filter(t => new Date(t.transactionDate).toISOString().split('T')[0] === today).reduce((sum, t) => sum + (t.price || 0), 0);
    const totalRevenue = transactions.reduce((sum, t) => sum + (t.price || 0), 0);

    document.getElementById('totalUsers').textContent = totalUsers;
    document.getElementById('expiryIn3Days').textContent = expiryIn3Days;
    document.getElementById('revenueToday').textContent = `₹${revenueToday.toLocaleString('en-IN')}`;
    document.getElementById('totalRevenue').textContent = `₹${totalRevenue.toLocaleString('en-IN')}`;

    const userDistCtx = document.getElementById('userDistChart').getContext('2d');
    new Chart(userDistCtx, {
        type: 'pie',
        data: {
            labels: ['Active Users', 'Inactive Users'],
            datasets: [{ data: [activeUsers, totalUsers - activeUsers], backgroundColor: ['#9c27b0', '#e1bee7'], borderColor: ['white', 'white'], borderWidth: 2 }]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'bottom' }, tooltip: { callbacks: { label: c => `${c.label}: ${c.raw} (${Math.round((c.raw / c.dataset.data.reduce((a, b) => a + b)) * 100)}%)` } } }
        }
    });

    const planExpiryTableBody = document.getElementById('planExpiryTableBody');
    const expiryData = transactions.filter(t => calculateDaysToExpiry(t.transactionDate, t.expiryDate) <= 3);
    planExpiryTableBody.innerHTML = expiryData.map(t => `
        <tr>
            <td>${t.transactionId}</td>
            <td>${t.username}</td>
            <td>${t.mobileNo}</td>
            <td>${t.planId}</td>
            <td>${t.planType}</td>
            <td class="${calculateDaysToExpiry(t.transactionDate, t.expiryDate) <= 3 ? 'expiry-warning' : ''}">${calculateDaysToExpiry(t.transactionDate, t.expiryDate)}</td>
        </tr>
    `).join('');

    const revenueGrowthCtx = document.getElementById('revenueGrowthChart').getContext('2d');
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const revenueByMonth = months.map(() => 0);
    transactions.forEach(t => {
        const date = new Date(t.transactionDate);
        if (date.getFullYear() === 2025) revenueByMonth[date.getMonth()] += t.price || 0;
    });
    const maxRevenue = Math.max(...revenueByMonth, 1500);
    const maxY = Math.ceil(maxRevenue / 1500) * 1500;

    new Chart(revenueGrowthCtx, {
        type: 'bar',
        data: { labels: months, datasets: [{ label: 'Revenue Growth (₹)', data: revenueByMonth, backgroundColor: '#9c27b0', borderColor: '#9c27b0', borderWidth: 1 }] },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true, max: maxY, ticks: { stepSize: 1500 }, title: { display: true, text: 'Price (₹)' } }, x: { title: { display: true, text: 'Months' } } },
            plugins: { legend: { position: 'top' } }
        }
    });
}

// User Management Functions
function populateUserTable(transactions) {
    const tableBody = document.querySelector("#userTable tbody");
    tableBody.innerHTML = "";
    let expiringUsersCount = 0;

    transactions.forEach(t => {
        const daysToExpiry = calculateDaysToExpiry(t.transactionDate, t.expiryDate);
        if (daysToExpiry <= 3) expiringUsersCount++;
        
        const paymentStatusClass = t.paymentStatus === 'SUCCESS' ? 'status-active' : 
                                 t.paymentStatus === 'FAILED' ? 'status-inactive' : 'status-pending';
        
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${t.transactionId}</td>
            <td>${t.username}</td>
            <td>${t.mobileNo}</td>
            <td>${t.planId}</td>
            <td>${t.planType}</td>
            <td>₹${t.price?.toFixed(2) || '0.00'}</td>
            <td>${t.validityDays}</td>
            <td>${formatIndianDate(t.transactionDate)}</td>
            <td>${formatIndianDate(t.expiryDate)}</td>
            <td class="${daysToExpiry <= 3 ? 'expiry-warning' : ''}">${daysToExpiry}</td>
            <td><span class="status-badge ${paymentStatusClass}">${t.paymentStatus || 'PENDING'}</span></td>
            <td><i class="bi bi-download" style="cursor: pointer;" onclick="downloadTransactionPDF('${t.transactionId}')" title="Download Receipt"></i></td>
        `;
        tableBody.appendChild(row);
    });

    document.getElementById("expiringUsers").textContent = expiringUsersCount;
}


// Add these new PDF download functions
async function downloadAllTransactionsPDF() {
    try {
        const transactions = await fetchTransactions();
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        // Add MobiComm header
        doc.setFontSize(20);
        doc.setTextColor(91, 46, 131); // MobiComm purple
        doc.text('MobiComm - Transaction Report', 105, 15, { align: 'center' });
        
        // Add report details
        doc.setFontSize(10);
        doc.setTextColor(0, 0, 0);
        doc.text(`Generated on: ${new Date().toLocaleDateString('en-IN')}`, 105, 25, { align: 'center' });
        
        // Prepare table data
        const headers = [
            "Txn ID",
            "Username",
            "Mobile",
            "Plan ID",
            "Plan Type",
            "Price",
            "Validity",
            "Txn Date",
            "Expiry",
            "Days Left",
            "Status"
        ];
        
        const data = transactions.map(t => [
            t.transactionId,
            t.username,
            t.mobileNo,
            t.planId,
            t.planType,
            `₹${t.price?.toFixed(2) || '0.00'}`,
            t.validityDays,
            formatIndianDate(t.transactionDate),
            formatIndianDate(t.expiryDate),
            calculateDaysToExpiry(t.transactionDate, t.expiryDate),
            t.paymentStatus || 'PENDING'
        ]);
        
        // Create table
        doc.autoTable({
            head: [headers],
            body: data,
            startY: 30,
            styles: {
                fontSize: 8,
                cellPadding: 3,
                overflow: 'linebreak'
            },
            headStyles: {
                fillColor: [91, 46, 131], // MobiComm purple
                textColor: 255,
                fontSize: 9,
                halign: 'center'
            },
            columnStyles: {
                0: { cellWidth: 15, halign: 'center' },
                1: { cellWidth: 20 },
                2: { cellWidth: 15, halign: 'center' },
                3: { cellWidth: 15, halign: 'center' },
                4: { cellWidth: 20 },
                5: { cellWidth: 15, halign: 'center' },
                6: { cellWidth: 15, halign: 'center' },
                7: { cellWidth: 20, halign: 'center' },
                8: { cellWidth: 20, halign: 'center' },
                9: { cellWidth: 15, halign: 'center' },
                10: { cellWidth: 15, halign: 'center' }
            },
            alternateRowStyles: {
                fillColor: [245, 245, 245]
            }
        });
        
        // Add footer
        const pageCount = doc.internal.getNumberOfPages();
        for(let i = 1; i <= pageCount; i++) {
            doc.setPage(i);
            doc.setFontSize(8);
            doc.text(`Page ${i} of ${pageCount}`, 105, doc.internal.pageSize.height - 10, { align: 'center' });
        }
        
        doc.save(`MobiComm_Transactions_${new Date().toISOString().split('T')[0]}.pdf`);
    } catch (error) {
        console.error("Error generating PDF:", error);
        alert("Failed to generate PDF report");
    }
}

function downloadTransactionPDF(transactionId) {
    fetchTransactions().then(transactions => {
        const t = transactions.find(t => t.transactionId == transactionId);
        if (!t) return;
        
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        // Add MobiComm header
        doc.setFontSize(20);
        doc.setTextColor(91, 46, 131); // MobiComm purple
        doc.text('MobiComm - Transaction Receipt', 105, 15, { align: 'center' });
        
        // Add transaction details
        doc.setFontSize(12);
        doc.setTextColor(0, 0, 0);
        
        let y = 30;
        doc.text(`Transaction ID: ${t.transactionId}`, 14, y);
        y += 8;
        doc.text(`Username: ${t.username}`, 14, y);
        y += 8;
        doc.text(`Mobile No: ${t.mobileNo}`, 14, y);
        y += 8;
        doc.text(`Plan ID: ${t.planId}`, 14, y);
        y += 8;
        doc.text(`Plan Type: ${t.planType}`, 14, y);
        y += 8;
        doc.text(`Price: ₹${t.price?.toFixed(2) || '0.00'}`, 14, y);
        y += 8;
        doc.text(`Validity: ${t.validityDays} days`, 14, y);
        y += 8;
        doc.text(`Transaction Date: ${formatIndianDate(t.transactionDate)}`, 14, y);
        y += 8;
        doc.text(`Expiry Date: ${formatIndianDate(t.expiryDate)}`, 14, y);
        y += 8;
        doc.text(`Days to Expiry: ${calculateDaysToExpiry(t.transactionDate, t.expiryDate)}`, 14, y);
        y += 8;
        doc.text(`Payment Status: ${t.paymentStatus || 'PENDING'}`, 14, y);
        
        // Add footer
        doc.setFontSize(10);
        doc.setTextColor(91, 46, 131);
        doc.text('Thank you for choosing MobiComm', 105, doc.internal.pageSize.height - 20, { align: 'center' });
        doc.setTextColor(0, 0, 0);
        doc.setFontSize(8);
        doc.text('This is an official transaction receipt from MobiComm', 105, doc.internal.pageSize.height - 15, { align: 'center' });
        
        doc.save(`MobiComm_Transaction_${t.transactionId}.pdf`);
    });
}

function updateUserCards(transactions) {
    document.getElementById("totalTransactions").textContent = transactions.length;
    document.getElementById("todayTransactions").textContent = transactions.filter(t => new Date(t.transactionDate).toISOString().split('T')[0] === new Date().toISOString().split('T')[0]).length;
}

function downloadTransactionData(transactionId) {
    fetchTransactions().then(transactions => {
        const t = transactions.find(t => t.transactionId === transactionId);
        if (!t) return;
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        doc.text(`Transaction ID: ${t.transactionId}`, 10, 10);
        doc.text(`Username: ${t.username}`, 10, 20);
        doc.text(`Mobile No: ${t.mobileNo}`, 10, 30);
        doc.text(`Plan ID: ${t.planId}`, 10, 40);
        doc.text(`Plan Type: ${t.planType}`, 10, 50);
        doc.text(`Price: ₹${t.price?.toFixed(2) || '0.00'}`, 10, 60);
        doc.text(`Validity: ${t.validityDays} days`, 10, 80);
        doc.text(`Transaction Date: ${formatIndianDate(t.transactionDate)}`, 10, 90);
        doc.text(`Expiry Date: ${formatIndianDate(t.expiryDate)}`, 10, 100);
        doc.text(`Days to Expiry: ${calculateDaysToExpiry(t.transactionDate, t.expiryDate)}`, 10, 110);
        doc.text(`Payment Status: ${t.paymentStatus || 'PENDING'}`, 10, 70);
        doc.save(`Transaction_${t.transactionId}.pdf`);
    });
}

function filterTransactionsByDate(transactions, filter) {
    const today = new Date();
    const yesterday = new Date(today); yesterday.setDate(today.getDate() - 1);
    switch (filter) {
        case "today": return transactions.filter(t => new Date(t.transactionDate).toDateString() === today.toDateString());
        case "yesterday": return transactions.filter(t => new Date(t.transactionDate).toDateString() === yesterday.toDateString());
        case "last3days": const threeDaysAgo = new Date(today); threeDaysAgo.setDate(today.getDate() - 3); return transactions.filter(t => new Date(t.transactionDate) >= threeDaysAgo);
        case "last10days": const tenDaysAgo = new Date(today); tenDaysAgo.setDate(today.getDate() - 10); return transactions.filter(t => new Date(t.transactionDate) >= tenDaysAgo);
        case "last1month": const oneMonthAgo = new Date(today); oneMonthAgo.setMonth(today.getMonth() - 1); return transactions.filter(t => new Date(t.transactionDate) >= oneMonthAgo);
        case "custom": return transactions;
        default: return transactions;
    }
}

function filterTransactionsBySearch(transactions, searchText) {
    return transactions.filter(t => Object.values(t).some(v => String(v).toLowerCase().includes(searchText.toLowerCase())));
}

async function updateUserManagement() {
    const transactions = await fetchTransactions();
    const dateFilter = document.getElementById("dateFilter");
    const searchInput = document.getElementById("searchInput");

    function applyFilters() {
        let filteredTransactions = filterTransactionsByDate(transactions, dateFilter.value);
        if (searchInput.value.trim()) filteredTransactions = filterTransactionsBySearch(filteredTransactions, searchInput.value.trim());
        populateUserTable(filteredTransactions);
        updateUserCards(filteredTransactions);
    }

    applyFilters();
    dateFilter.onchange = applyFilters;
    searchInput.oninput = applyFilters;
}

// Plan Management Functions
function renderPlans(plans) {
    const tbody = document.getElementById('plansTableBody');
    tbody.innerHTML = '';
    plans.forEach(plan => {
        const tr = document.createElement('tr');
        const status = plan.status.toUpperCase();
        const statusColor = status === 'ACTIVE' ? 'green' : 'red';
        tr.innerHTML = `
            <td>${plan.planId}</td>
            <td>${plan.category}</td>
            <td>${plan.dataLimit}</td>
            <td>${plan.validity} days</td>
            <td>${plan.service}</td>
            <td>₹${plan.price}</td>
            <td><span class="status" style="color: ${statusColor};">${status}</span></td>
            <td>
                <button class="action-btn" onclick="editPlan('${plan.planId}')">Edit</button>
                <button class="action-btn" onclick="changePlanStatus('${plan.planId}', '${status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}')">Change Status</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function changePlanStatus(planId, newStatus) {
    try {
        const response = await fetch(`${BASE_URL}/plans/${planId}/status?status=${newStatus.toUpperCase()}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });
        if (response.ok) fetchPlans().then(renderPlans);
        else console.error("Failed to change plan status:", response.statusText);
    } catch (error) {
        console.error("Error changing plan status:", error);
    }
}

function populateCategoryDropdown(categories) {
    const categoryDropdown = document.getElementById('category');
    categoryDropdown.innerHTML = '<option value="">Select Category</option>';
    categories.forEach(c => {
        const option = document.createElement('option');
        option.value = c.categoryId;
        option.textContent = c.categoryName;
        categoryDropdown.appendChild(option);
    });
}

function populateCategoryFilter(categories) {
    const categoryFilter = document.getElementById('categoryFilter');
    categoryFilter.innerHTML = '<option value="">Category</option>';
    categories.forEach(c => {
        const option = document.createElement('option');
        option.value = c.categoryId;
        option.textContent = c.categoryName;
        categoryFilter.appendChild(option);
    });
}

async function filterPlansByCategory() {
    const categoryId = document.getElementById('categoryFilter').value;
    if (categoryId === "") fetchPlans().then(renderPlans);
    else {
        try {
            const response = await fetch(`${BASE_URL}/plans/category/${categoryId}`);
            renderPlans(await response.json());
        } catch (error) {
            console.error("Error filtering plans by category:", error);
        }
    }
}

async function filterPlansByStatus() {
    const status = document.getElementById('statusFilter').value;
    if (status === "") fetchPlans().then(renderPlans);
    else {
        try {
            const response = await fetch(`${BASE_URL}/plans/status/${status}`);
            renderPlans(await response.json());
        } catch (error) {
            console.error("Error filtering plans by status:", error);
        }
    }
}

function searchPlans() {
    const query = document.getElementById('planSearchInput').value.toLowerCase();
    fetchPlans().then(plans => {
        const filteredPlans = plans.filter(p =>
            p.planId.toLowerCase().includes(query) || p.category.toLowerCase().includes(query) ||
            p.dataLimit.toString().includes(query) || p.validity.toString().includes(query) ||
            p.service.toLowerCase().includes(query) || p.price.toString().includes(query) ||
            p.status.toLowerCase().includes(query)
        );
        renderPlans(filteredPlans);
    });
}

async function savePlan(event) {
    event.preventDefault();
    const planId = document.getElementById('planId').value;
    const categoryId = document.getElementById('category').value;
    const dataLimit = document.getElementById('data').value;
    const validity = document.getElementById('validity').value;
    const service = document.getElementById('service').value;
    const price = document.getElementById('price').value;
    const modalTitle = document.getElementById('modalTitle').textContent;

    if (!categoryId) return displayCategoryError("Please select a valid category.");

    let status = 'ACTIVE';
    if (modalTitle === 'Edit Plan') {
        const plans = await fetchPlans();
        const currentPlan = plans.find(p => p.planId === planId);
        if (currentPlan) status = currentPlan.status;
    }

    const plan = { planId, category: { categoryId }, dataLimit, validity, service, price, status };
    try {
        const isEdit = modalTitle === 'Edit Plan';
        const response = await fetch(isEdit ? `${BASE_URL}/plans/${planId}` : `${BASE_URL}/plans`, {
            method: isEdit ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(plan)
        });
        if (response.ok) {
            closeModal();
            fetchPlans().then(renderPlans);
        } else {
            const errorData = await response.json();
            displayCategoryError(errorData.message || `Failed to ${isEdit ? 'update' : 'add'} plan.`);
        }
    } catch (error) {
        console.error(`Error ${modalTitle === 'Edit Plan' ? 'updating' : 'adding'} plan:`, error);
        displayCategoryError("An error occurred.");
    }
}

function displayCategoryError(message) {
    const errorElement = document.getElementById('categoryError');
    errorElement.textContent = message;
    errorElement.style.display = 'block';
}

async function editPlan(planId) {
    const plans = await fetchPlans();
    const plan = plans.find(p => p.planId === planId);
    if (plan) {
        document.getElementById('modalTitle').textContent = 'Edit Plan';
        document.getElementById('planId').value = plan.planId;
        document.getElementById('category').value = plan.category; // category is categoryId string
        document.getElementById('data').value = plan.dataLimit;
        document.getElementById('validity').value = plan.validity;
        document.getElementById('service').value = plan.service;
        document.getElementById('price').value = plan.price;
        document.getElementById('planId').disabled = true;
        document.getElementById('planModal').style.display = 'block';
    }
}

function openAddModal() {
    document.getElementById('modalTitle').textContent = 'Add New Plan';
    document.getElementById('planForm').reset();
    document.getElementById('planId').disabled = false;
    document.getElementById('categoryError').style.display = 'none';
    document.getElementById('planModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('planModal').style.display = 'none';
}

function openAddCategoryModal() {
    document.getElementById('categoryForm').reset();
    document.getElementById('categoryModal').style.display = 'block';
}

function closeCategoryModal() {
    document.getElementById('categoryModal').style.display = 'none';
}

async function saveCategory(event) {
    event.preventDefault();
    const categoryId = document.getElementById('categoryId').value;
    const categoryName = document.getElementById('categoryName').value;
    const category = { categoryId, categoryName };
    try {
        const response = await fetch(`${BASE_URL}/categories`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(category)
        });
        if (response.ok) {
            closeCategoryModal();
            fetchCategories().then(categories => {
                populateCategoryDropdown(categories);
                populateCategoryFilter(categories);
            });
            fetchPlans().then(renderPlans);
        } else {
            console.error("Failed to add category:", await response.text());
        }
    } catch (error) {
        console.error("Error adding category:", error);
    }
}

async function updatePlanManagement() {
    fetchPlans().then(renderPlans);
    const categories = await fetchCategories();
    populateCategoryDropdown(categories);
    populateCategoryFilter(categories);
}

// Reports & Analytics Functions
async function deleteFeedback(tokenId) {
    try {
        const response = await fetch(`${BASE_URL}/support/${tokenId}`, { method: "DELETE" });
        if (!response.ok) throw new Error("Failed to delete feedback");
        return await response.text();
    } catch (error) {
        console.error("Error deleting feedback:", error);
        return null;
    }
}

function populateFeedbackTable(feedbackList) {
    const tableBody = document.getElementById("feedbackTableBody");
    tableBody.innerHTML = "";
    feedbackList.forEach(f => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${f.tokenId || "N/A"}</td>
            <td>${f.username || "N/A"}</td>
            <td>${f.mobileNo || "N/A"}</td>
            <td><button class="btn btn-open" onclick="showMessage('${f.message || "No message"}', ${f.tokenId})">Open</button></td>
            <td><button class="btn btn-delete" onclick="showDeleteConfirm(${f.tokenId})">Delete</button></td>
        `;
        tableBody.appendChild(row);
    });
}

function filterFeedback(feedbackList, searchText) {
    return feedbackList.filter(f =>
        (f.username && f.username.toLowerCase().includes(searchText.toLowerCase())) ||
        (f.mobileNo && f.mobileNo.includes(searchText))
    );
}

function showMessage(message, tokenId) {
    document.getElementById("modalMessageContent").textContent = message;
    const modal = new bootstrap.Modal(document.getElementById("messageModal"));
    modal.show();
}

function showDeleteConfirm(tokenId) {
    const modal = new bootstrap.Modal(document.getElementById("deleteConfirmModal"));
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    confirmDeleteBtn.onclick = async () => {
        if (await deleteFeedback(tokenId)) {
            modal.hide();
            updateReportsAnalytics();
        } else {
            alert("Failed to delete feedback.");
        }
    };
    modal.show();
}

async function updateReportsAnalytics() {
    const feedbackList = await fetchFeedback();
    const searchInput = document.getElementById("feedbackSearchInput");

    function applyFilters() {
        const filteredFeedback = filterFeedback(feedbackList, searchInput.value.trim());
        populateFeedbackTable(filteredFeedback);
    }

    applyFilters();
    searchInput.oninput = applyFilters;
}

// User Profile Functions
let users = [];

function populateUserProfileTable(data) {
    const tableBody = document.getElementById('userTableBody');
    tableBody.innerHTML = "";
    data.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.username}</td>
            <td>${user.mobileNo}</td>
            <td>${user.email}</td>
            <td>${user.location}</td>
            <td><span class="status-badge ${user.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}">${user.status}</span></td>
            <td><button class="action-btn" onclick="changeUserStatus('${user.mobileNo}', '${user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}')">Change Status</button></td>
            <td><i class="fas fa-download download-icon" onclick="downloadUserPDF('${user.username}')"></i></td>
        `;
        tableBody.appendChild(row);
    });
    updateStatsCards(data);
}

async function changeUserStatus(mobileNo, newStatus) {
    try {
        const response = await fetch(`${BASE_URL}/users/status?mobileNo=${mobileNo}&status=${newStatus}`, { method: 'PUT' });
        if (response.ok) {
            const updatedUser = await response.json();
            const userIndex = users.findIndex(u => u.mobileNo === mobileNo);
            if (userIndex !== -1) {
                users[userIndex].status = updatedUser.status;
                populateUserProfileTable(users);
            }
        } else {
            throw new Error('Failed to update user status');
        }
    } catch (error) {
        console.error('Error changing user status:', error);
    }
}

function downloadUserPDF(username) {
    const user = users.find(u => u.username === username);
    if (user) {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        doc.text(`Username: ${user.username}`, 10, 10);
        doc.text(`Mobile No: ${user.mobileNo}`, 10, 20);
        doc.text(`Email: ${user.email}`, 10, 30);
        doc.text(`Location: ${user.location}`, 10, 40);
        doc.text(`Status: ${user.status}`, 10, 50);
        doc.save(`user_${user.username}.pdf`);
    }
}

function downloadAllUsersExcel() {
    const worksheet = XLSX.utils.json_to_sheet(users);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Users");
    XLSX.writeFile(workbook, "users.xlsx");
}

function updateStatsCards(data) {
    const totalUsers = data.length;
    const activeUsers = data.filter(u => u.status === "ACTIVE").length;
    const inactiveUsers = data.filter(u => u.status === "INACTIVE").length;
    const locations = data.map(u => u.location);
    const mostUsersLocation = locations.sort((a, b) => locations.filter(v => v === a).length - locations.filter(v => v === b).length).pop();

    document.getElementById('statsCards').innerHTML = `
        <div class="col-md-3 col-sm-6 mb-3">
            <div class="card stat-card"><div class="stat-label">Total Users</div><div class="stat-number">${totalUsers}</div></div>
        </div>
        <div class="col-md-3 col-sm-6 mb-3">
            <div class="card stat-card"><div class="stat-label">Active Users</div><div class="stat-number">${activeUsers}</div></div>
        </div>
        <div class="col-md-3 col-sm-6 mb-3">
            <div class="card stat-card"><div class="stat-label">Inactive Users</div><div class="stat-number">${inactiveUsers}</div></div>
        </div>
        <div class="col-md-3 col-sm-6 mb-3">
            <div class="card stat-card"><div class="stat-label">Most Users Location</div><div class="stat-number">${mostUsersLocation}</div></div>
        </div>
    `;
}

async function updateUserProfile() {
    users = await fetchUsers();
    const statusFilter = document.getElementById('userStatusFilter');
    const searchInput = document.getElementById('userSearchInput');
    const downloadTableIcon = document.getElementById('downloadTable');

    function applyFilters() {
        let filteredUsers = users;
        if (statusFilter.value !== "Filter by Status") filteredUsers = filteredUsers.filter(u => u.status === statusFilter.value.toUpperCase());
        if (searchInput.value.trim()) filteredUsers = filteredUsers.filter(u =>
            u.username.toLowerCase().includes(searchInput.value.toLowerCase()) ||
            u.email.toLowerCase().includes(searchInput.value.toLowerCase()) ||
            u.location.toLowerCase().includes(searchInput.value.toLowerCase())
        );
        populateUserProfileTable(filteredUsers);
    }

    applyFilters();
    statusFilter.onchange = applyFilters;
    searchInput.oninput = applyFilters;
    downloadTableIcon.onclick = downloadAllUsersExcel;
}

// SPA Routing
function showView(viewId) {
    document.querySelectorAll('.view').forEach(view => view.style.display = view.id === `${viewId}-view` ? 'block' : 'none');
    document.querySelectorAll('.nav-link').forEach(link => link.classList.toggle('active', link.dataset.view === viewId));
    document.getElementById('navbarTitle').textContent = {
        'dashboard': 'Admin Dashboard',
        'user-management': 'User Management',
        'plan-management': 'Plan Management',
        'reports-analytics': 'Reports & Analytics',
        'user-profile': 'User Profile'
    }[viewId];

    if (viewId === 'dashboard') updateDashboard();
    else if (viewId === 'user-management') updateUserManagement();
    else if (viewId === 'plan-management') updatePlanManagement();
    else if (viewId === 'reports-analytics') updateReportsAnalytics();
    else if (viewId === 'user-profile') updateUserProfile();
}

// Initialize SPA
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            showView(link.dataset.view);
        });
    });
    setAdminUsername(); // Set username on page load
    showView('dashboard');

    setInterval(() => {
        if (document.getElementById('dashboard-view').style.display === 'block') updateDashboard();
        if (document.getElementById('reports-analytics-view').style.display === 'block') updateReportsAnalytics();
    }, 300000); // 5 minutes
    setInterval(() => {
        if (document.getElementById('user-management-view').style.display === 'block') updateUserManagement();
    }, 86400000); // 24 hours
});