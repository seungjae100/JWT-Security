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
};