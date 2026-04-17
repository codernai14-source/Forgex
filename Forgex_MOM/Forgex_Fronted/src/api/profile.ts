import http from './http'

/**
 * 个人中心页面使用的资料相关接口。
 */

/**
 * 获取当前登录用户的资料信息。
 */
export async function getCurrentUserInfo() {
  return http.post('/sys/profile/get', {})
}

/**
 * 更新用户可编辑的基础资料，例如姓名、联系方式、头像等。
 */
export async function updateBasicInfo(data: any) {
  return http.post('/sys/profile/updateBasic', data)
}

/**
 * 修改当前登录用户的密码。
 */
export async function changePassword(data: { oldPassword: string; newPassword: string }) {
  return http.post('/sys/profile/changePassword', data)
}

/**
 * 上传当前用户的新头像文件。
 */
export async function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<string>('/sys/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
