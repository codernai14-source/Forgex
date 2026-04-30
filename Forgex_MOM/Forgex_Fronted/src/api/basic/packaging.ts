import http from '@/api/http';

export function getPackagingTypePage(data: any) {
  return http.post('/basic/packaging/page', data);
}

export function getPackagingTypeDetail(id: number) {
  return http.post('/basic/packaging/detail', { id });
}

export function createPackagingType(data: any) {
  return http.post('/basic/packaging/create', data);
}

export function updatePackagingType(data: any) {
  return http.post('/basic/packaging/update', data);
}

export function deletePackagingType(id: number) {
  return http.post('/basic/packaging/delete', { id });
}

export function batchDeletePackagingType(ids: number[]) {
  return http.post('/basic/packaging/batchDelete', { ids });
}

export function getAllPackagingTypes() {
  return http.post('/basic/packaging/list');
}
