/**
 * 个人信息 API
 */
import http from './http';
/**
 * 获取当前用户信息
 */
export async function getCurrentUserInfo() {
    return http.post('/sys/profile/get', {});
}
/**
 * 更新基本信息
 */
export async function updateBasicInfo(data) {
    return http.post('/sys/profile/updateBasic', data);
}
/**
 * 修改密码
 */
export async function changePassword(data) {
    return http.post('/sys/profile/changePassword', data);
}
/**
 * 上传头像
 */
export async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    return http.post('/sys/file/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}
