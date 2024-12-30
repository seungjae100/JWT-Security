const AuthService = {
    async login(email, password) {
        try {
            const data = await ApiService.post('/auth/login', {email, password});
            TokenService.setAccessToken(data.accessToken);
            TokenService.setUserInfo(data.email, data.name);
            return data;
        } catch (error) {
            console.error('로그인 에러: ', error);
            throw error;
        }
    },

    async register(email, password, name) {
        try {
            const data = await ApiService.post('/auth/register', {email, password, name});
            TokenService.setAccessToken(data.accessToken);
            TokenService.setUserInfo(data.email, data.name);
            return data;
        } catch (error) {
            console.error('회원가입 에러: ', error);
            throw error;
        }
    },

    async logout() {
        try {
            const token = TokenService.getAccessToken();
            if (!token) {
                throw new Error('로그인 상태가 아닙니다.');
            }

            await ApiService.post('/auth/logout', {}, {
                Authorization: `Bearer ${token}`  
            });
            
            TokenService.removeAccessToken();
            TokenService.removeUserInfo();
        } catch (error) {
            console.error('로그아웃 에러: ', error);
            throw error;
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
        window.location.href = '/';
    } catch (error) {
        alert(error.message);
    }
});