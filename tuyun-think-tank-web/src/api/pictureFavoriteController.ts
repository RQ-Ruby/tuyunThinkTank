// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 收藏图片 POST /api/picture/favorite/add/{pictureId} */
export async function addFavoriteUsingPost(
  pictureId: string | number,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>(`/api/picture/favorite/add/${pictureId}`, {
    method: 'POST',
    ...(options || {}),
  })
}

/** 取消收藏 POST /api/picture/favorite/remove/{pictureId} */
export async function removeFavoriteUsingPost(
  pictureId: string | number,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>(`/api/picture/favorite/remove/${pictureId}`, {
    method: 'POST',
    ...(options || {}),
  })
}

/** 检查是否已收藏 GET /api/picture/favorite/check/{pictureId} */
export async function checkFavoriteUsingGet(
  pictureId: string | number,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>(`/api/picture/favorite/check/${pictureId}`, {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取我的收藏列表 POST /api/picture/favorite/my/list/page */
export async function listMyFavoritePictureByPageUsingPost(
  body: API.PictureQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePictureVO_>('/api/picture/favorite/my/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}