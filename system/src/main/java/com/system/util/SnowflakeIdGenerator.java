package com.system.util;

/**
 * 雪花算法 ID 生成器
 */
public class SnowflakeIdGenerator {
    // 起始时间戳 (2023-01-01 00:00:00)
    private static final long START_TIMESTAMP = 1672502400000L;
    // 机器ID所占的位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据标识ID所占的位数
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 支持的最大机器ID (0-31)
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 支持的最大数据标识ID (0-31)
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    // 序列在ID中占的位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器ID向左移12位
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据标识ID向左移17位 (12+5)
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    // 时间戳向左移22位 (5+5+12)
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    // 生成序列的掩码，这里为4095 (0b111111111111)
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static long workerId;         // 机器ID
    private static long dataCenterId;     // 数据中心ID
    private static long sequence = 0L;    // 毫秒内序列
    private static long lastTimestamp = -1L; // 上次生成ID的时间戳

    // 静态初始化
    static {
        // 根据部署环境动态获取机器ID和数据中心ID
        workerId = 1L;
        dataCenterId = 1L;

        // 校验机器ID和数据中心ID
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("dataCenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
    }

    /**
     * 生成下一个ID
     */
    public static synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间戳
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}

