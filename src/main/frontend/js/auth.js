const AuthService = {
    async login(email, password) {
        try {
            const data = await ApiService.post('/auth/login', {email, password});
            TokenService.setAccessToken(data.accessToken);
            return data;
        } catch (error) {
            throw new Error('로그인에 실패했습니다.');
        }
    },

    async register(email, password, name) {
        try {
            const data = await ApiService.post('/auth/register', {email, password, name});
            TokenService.setAccessToken(data.accessToken);
            return data;
        } catch (error) {
            throw new Error('회원가입에 실패했습니다.');
        }
    },

    async logout() {
        try {
            const token = TokenService.getAccessToken();
            if (!token) {
                console.warn('로그인 상태가 아닙니다.');
                return;
            }

            await ApiService.post('/auth/logout', {}, {
                Authorization: `Bearer ${token}`  
            });
            
            TokenService.removeAccessToken();
        } catch (error) {
            console.error('로그아웃 요청 중 오류: ', error.message);
        }
    }
};

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

document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        await AuthService.register(
            document.getElementById('email').value,
            document.getElementById('password').value,
            document.getElementById('name').value
        );
        window.location.href = 'login.html';
    } catch (error) {
        alert(error.message);
    }
});

document.getElementById('logoutButton')?.addEventListener('click', async () => {
    try {
        await AuthService.logout();
        window.location.href = 'index.html';
    } catch (error) {
        console.error('로그아웃 중 오류:', error.message);
        alert(error.message);
    }
});