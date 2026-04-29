import request from '@/utils/request';

export function getUnitPage(data: any) {
  return request({
    url: '/basic/unit/page',
    method: 'post',
    data,
  });
}

export function getUnitDetail(id: number) {
  return request({
    url: '/basic/unit/detail',
    method: 'post',
    data: { id },
  });
}

export function createUnit(data: any) {
  return request({
    url: '/basic/unit/create',
    method: 'post',
    data,
  });
}

export function updateUnit(data: any) {
  return request({
    url: '/basic/unit/update',
    method: 'post',
    data,
  });
}

export function deleteUnit(id: number) {
  return request({
    url: '/basic/unit/delete',
    method: 'post',
    data: { id },
  });
}

export function batchDeleteUnit(ids: number[]) {
  return request({
    url: '/basic/unit/batchDelete',
    method: 'post',
    data: { ids },
  });
}

export function getAllUnits() {
  return request({
    url: '/basic/unit/list',
    method: 'post',
  });
}
