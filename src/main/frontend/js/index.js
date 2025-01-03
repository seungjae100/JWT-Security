document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
});

function checkAuthStatus() {
    const token = TokenService.getAccessToken();
    const userInfo = document.getElementById('userInfo');
    const authButtons = document.getElementById('authButtons');
    const authenticatedContent = document.getElementById('authenticatedContent');

    if (token) {
        // 로그인 상태
        document.getElementById('userName').textContent = '사용자';
        userInfo.style.display = 'flex';
        authButtons.style.display = 'none';
        authenticatedContent.style.display = 'block';
    } else {
        // 비로그인 상태
        userInfo.style.display = 'none';
        authButtons.style.display = 'flex';
        authenticatedContent.style.display = 'none';
    }
}

async function handleLogout() {
    try {
        await AuthService.logout();
        window.location.reload();
    } catch (error) {
        console.error('로그아웃 중 오류:', error.message);
        alert('로그아웃 중 오류가 발생했습니다.');
    }
}