document.getElementById('sidebarToggle').addEventListener('click', function() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('active');
    
    const icon = this.querySelector('i');
    if (sidebar.classList.contains('active')) {
        icon.classList.remove('fa-bars');
        icon.classList.add('fa-times');
    } else {
        icon.classList.remove('fa-times');
        icon.classList.add('fa-bars');
    }
});

 document.getElementById('sidebarClose').addEventListener('click', function() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.remove('active');
    
    const toggleIcon = document.querySelector('#sidebarToggle i');
    toggleIcon.classList.remove('fa-times');
    toggleIcon.classList.add('fa-bars');
});

function adjustLayout() {
    const content = document.getElementById('content');
    const sidebar = document.getElementById('sidebar');
    
    if (window.innerWidth > 576) {
        content.style.marginLeft = window.innerWidth <= 768 ? '80px' : '250px';
    } else {
        content.style.marginLeft = sidebar.classList.contains('active') ? '250px' : '0';
    }
}
window.addEventListener('resize', adjustLayout);
adjustLayout();