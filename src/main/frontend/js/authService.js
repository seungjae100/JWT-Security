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
            TokenService.setAccessToken(data.accessToken());
            TokenService.setUserInfo(data.email, data.name);
            return data;
        } catch (error) {
            console.error('회원가입 에러: ', error);
            throw error;
        }
    },

    async logout() {
        try {
            await ApiService.post('/auth/logout', {}, TokenService.getAccessToken());
            TokenService.removeAccessToken();
            TokenService.removeUserInfo();
        } catch (error) {
            console.error('로그아웃 에러: ', error);
            throw error;
        }
    }
};