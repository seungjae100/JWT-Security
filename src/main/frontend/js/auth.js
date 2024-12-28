document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await AuthService.login(
            document.getElementById('email').value,
            document.getElementById('password').value
        );
        window.location.href = 'index.html';
    } catch (error) {
        alert(error.message);
    }
});