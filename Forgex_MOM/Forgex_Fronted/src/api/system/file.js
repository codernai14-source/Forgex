import http from '../http';
/**
 * 上传文件
 * @param file 文件对象
 * @param config 请求配置选项
 * @returns 上传结果
 */
export function uploadFile(file, config = {}) {
    const fd = new FormData();
    fd.append('file', file);
    return http.post('/sys/file/upload', fd, {
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        ...config
    });
}
