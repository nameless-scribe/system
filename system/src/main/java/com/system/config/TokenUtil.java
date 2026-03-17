package com.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 简单的 Token 工具类：
 * 结构：Base64.encode(userId:expireTime:sign)
 * sign = MD5(userId + ":" + expireTime + ":" + SECRET)
 *
 * 有效期：30 分钟
 */
public class TokenUtil {

    private static final Logger log = LoggerFactory.getLogger(TokenUtil.class);

    /**
     * 签名密钥：
     * 优先从 JVM 参数 / 环境变量 读取，未配置时退回默认值。
     * -Dapp.token.secret=xxx 或 环境变量 APP_TOKEN_SECRET
     */
    private static final String SECRET;

    /**
     * 默认有效期（毫秒），默认 30 分钟
     * 可通过 JVM 参数 -Dapp.token.expireMinutes=60 或环境变量 APP_TOKEN_EXPIRE_MINUTES 覆盖
     */
    private static final long EXPIRE_MILLIS;

    static {
        String fromJvmSecret = System.getProperty("app.token.secret");
        String fromEnvSecret = System.getenv("APP_TOKEN_SECRET");
        String sec = fromJvmSecret != null && !fromJvmSecret.isEmpty()
                ? fromJvmSecret
                : (fromEnvSecret != null && !fromEnvSecret.isEmpty() ? fromEnvSecret : "change-this-secret-2026");
        SECRET = sec;

        long defaultMinutes = 30L;
        long minutes = defaultMinutes;
        String fromJvmExpire = System.getProperty("app.token.expireMinutes");
        String fromEnvExpire = System.getenv("APP_TOKEN_EXPIRE_MINUTES");
        String expireRaw = fromJvmExpire != null && !fromJvmExpire.isEmpty()
                ? fromJvmExpire
                : (fromEnvExpire != null && !fromEnvExpire.isEmpty() ? fromEnvExpire : null);
        if (expireRaw != null) {
            try {
                minutes = Long.parseLong(expireRaw);
            } catch (NumberFormatException e) {
                log.warn("解析 token 过期时间失败，使用默认 {} 分钟，raw={}", defaultMinutes, expireRaw);
                minutes = defaultMinutes;
            }
        }
        EXPIRE_MILLIS = minutes * 60 * 1000L;
        log.info("TokenUtil 初始化完成，过期时间={} 分钟", minutes);
    }

    /**
     * 生成 token，携带用户 id 与过期时间
     */
    public static String generateToken(Long userId) {
        long expireTime = System.currentTimeMillis() + EXPIRE_MILLIS;
        String payload = userId + ":" + expireTime;
        String sign = md5(payload + ":" + SECRET);
        String tokenRaw = payload + ":" + sign;
        return Base64.getEncoder().encodeToString(tokenRaw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析并校验 token，返回用户 id；无效或过期返回 null
     */
    public static Long parseToken(String token) {
        try {
            byte[] decoded = Base64.getDecoder().decode(token);
            String tokenRaw = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = tokenRaw.split(":");
            if (parts.length != 3) {
                return null;
            }
            Long userId = Long.parseLong(parts[0]);
            long expireTime = Long.parseLong(parts[1]);
            String sign = parts[2];

            if (expireTime < System.currentTimeMillis()) {
                // 已过期
                log.debug("解析 token 失败：已过期");
                return null;
            }

            String payload = parts[0] + ":" + parts[1];
            String expectSign = md5(payload + ":" + SECRET);
            if (!expectSign.equalsIgnoreCase(sign)) {
                log.warn("解析 token 失败：签名不匹配");
                return null;
            }
            return userId;
        } catch (Exception e) {
            log.warn("解析 token 异常：{}", e.getMessage());
            return null;
        }
    }

    private static String md5(String text) {
        // 复用 Spring 的 MD5 工具
        return org.springframework.util.DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
}

