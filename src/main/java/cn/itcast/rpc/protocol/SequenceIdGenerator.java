package cn.itcast.rpc.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Id生成计数器
 */
public abstract class SequenceIdGenerator {

    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }

}
