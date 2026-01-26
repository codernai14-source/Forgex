import http from '../http';
export function login(data) {
    return http.post('/auth/login', data);
}
export function chooseTenant(data) {
    return http.post('/auth/choose-tenant', data);
}
export function getPublicKey() {
    return http.get('/auth/crypto/public-key');
}
export function updateTenantPreferences(data) {
    return http.post('/auth/tenant/preferences', data);
}
export function changeLanguage(data) {
    return http.post('/auth/changeLanguage', data);
}
export function logout() {
    return http.post('/auth/logout');
}
export function getSocialAuthorizeUrl(platform) {
    return http.get('/auth/social/authorizeUrl', { params: { platform } });
}
