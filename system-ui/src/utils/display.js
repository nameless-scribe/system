// 展示相关的通用方法

/**
 * 商品成色（新旧程度）文案
 */
export function conditionText (level) {
  switch (level) {
    case 1: return '全新'
    case 2: return '9成新'
    case 3: return '8成新'
    case 4: return '7成新及以下'
    default: return '未知成色'
  }
}

