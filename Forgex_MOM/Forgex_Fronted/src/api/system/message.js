import http from '../http';
export function sendMessage(dto) {
    return http.post('/sys/message/send', dto);
}
export function listUnreadMessages(limit = 20) {
    return http.get('/sys/message/unread', { params: { limit } });
}
export function markMessageRead(id) {
    return http.post('/sys/message/read', { id });
}
