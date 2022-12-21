package cn.itcast.netty_basic.bytebuf;//package cn.itcast.netty_basic.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * <h2>ByteBuf与内存池化</h2>
 * Netty的4.1.x版本之后的ByteBuf默认开启池化功能
 */
public class TestByteBuf_2 {

    public static void main(String[] args) {

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        // 池化直接内存
        // class io.netty.buffer.PooledUnsafeDirectByteBuf
        System.out.println(buf.getClass());
    }

}
