import http from '@/api/http';

export function getUnitPage(data: any) {
  return http.post('/basic/unit/page', data);
}

export function getUnitDetail(id: number) {
  return http.post('/basic/unit/detail', { id });
}

export function createUnit(data: any) {
  return http.post('/basic/unit/create', data);
}

export function updateUnit(data: any) {
  return http.post('/basic/unit/update', data);
}

export function deleteUnit(id: number) {
  return http.post('/basic/unit/delete', { id });
}

export function batchDeleteUnit(ids: number[]) {
  return http.post('/basic/unit/batchDelete', { ids });
}

export function getAllUnits() {
  return http.post('/basic/unit/list');
}
