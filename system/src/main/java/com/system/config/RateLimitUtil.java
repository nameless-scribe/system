package com.system.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 非分布式环境下的简单限流工具：
 * 基于内存的滑动时间窗口，对某个 key（如 IP、用户名+IP）在固定时间内的请求次数做限制。
 *
 * 适用场景：登录、修改密码等敏感接口的防暴力尝试。
 */
public class RateLimitUtil {

    private static final Map<String, Window> WINDOW_MAP = new ConcurrentHashMap<>();

    private RateLimitUtil() {
    }

    /**
     * 尝试获取一次访问权限。
     *
     * @param key         限流 key，例如 "login:username:ip"
     * @param maxCount    时间窗口内允许的最大次数
     * @param windowMills 时间窗口大小（毫秒）
     * @return true 表示允许访问；false 表示超过限制
     */
    public static boolean tryAcquire(String key, int maxCount, long windowMills) {
        long now = System.currentTimeMillis();
        Window win = WINDOW_MAP.compute(key, (k, old) -> {
            if (old == null || now - old.windowStart > windowMills) {
                // 窗口过期或首次访问，重置
                return new Window(now, 1);
            }
            if (old.count >= maxCount) {
                // 已经达到上限，保持计数不变
                return old;
            }
            old.count += 1;
            return old;
        });
        if (win == null) {
          // 理论上不会发生，保险返回允许
          return true;
        }
        // 如果当前窗口内计数 > maxCount，则视为不允许
        return win.count <= maxCount;
    }

    private static class Window {
        final long windowStart;
        int count;

        Window(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

