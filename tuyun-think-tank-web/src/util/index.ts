import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 * @param size
 */
export const formatSize = (size?: number) => {
  if (!size) return '未知'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}
/**
 * 下载图片
 * @param url 图片下载地址
 * @param fileName 要保存为的文件名
 */
export function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  saveAs(url, fileName)
}

/**
 * 转换颜色为 Hex 格式
 * @param color
 */
export function toHexColor(color?: string) {
  if (!color) return ''
  if (color.startsWith('0x')) {
    return '#' + color.substring(2)
  }
  // 如果已经是 # 开头，直接返回
  if (color.startsWith('#')) {
    return color
  }
  // 否则加上 #
  return '#' + color
}
