const TokenService = {
    setAccessToken(token) {
        sessionStorage.setItem('accessToken', token);
    },

    getAccessToken() {
        return sessionStorage.getItem('accessToken');
    },

    removeAccessToken() {
        sessionStorage.removeItem('accessToken');
    },

    setUserInfo(email, name) {
        sessionStorage.setItem('userEmail', email);
        sessionStorage.setItem('userName', name);
    },

    removeUserInfo() {
        sessionStorage.removeItem('userEmail');
        sessionStorage.removeItem('userName');
    }
};