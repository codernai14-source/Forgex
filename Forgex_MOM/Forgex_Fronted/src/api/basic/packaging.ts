import request from '@/utils/request';

export function getPackagingTypePage(data: any) {
  return request({
    url: '/basic/packaging/page',
    method: 'post',
    data,
  });
}

export function getPackagingTypeDetail(id: number) {
  return request({
    url: '/basic/packaging/detail',
    method: 'post',
    data: { id },
  });
}

export function createPackagingType(data: any) {
  return request({
    url: '/basic/packaging/create',
    method: 'post',
    data,
  });
}

export function updatePackagingType(data: any) {
  return request({
    url: '/basic/packaging/update',
    method: 'post',
    data,
  });
}

export function deletePackagingType(id: number) {
  return request({
    url: '/basic/packaging/delete',
    method: 'post',
    data: { id },
  });
}

export function batchDeletePackagingType(ids: number[]) {
  return request({
    url: '/basic/packaging/batchDelete',
    method: 'post',
    data: { ids },
  });
}

export function getAllPackagingTypes() {
  return request({
    url: '/basic/packaging/list',
    method: 'post',
  });
}
