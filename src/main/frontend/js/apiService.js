const API_URL = 'http://localhost:8080';

const ApiService = {
    async post(endpoint, data, headers = {}) {
        const defaultHeaders = {
            'Content-Type': 'application/json',
            ...headers
        };

        const response = await fetch(`${API_URL}${endpoint}`, {
            method: 'POST',
            headers: defaultHeaders,
            credentials: 'include',
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            if (response.status === 403 ) {
                throw new Error('접근 권한이 없습니다.');
            }
            const error = await response.json();
            throw new Error(error.message);
        }

        // 응답 본문이 없을 수 있으므로 조건부로 JSON 파싱
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return response.json();
        }
        return null;
    }
};