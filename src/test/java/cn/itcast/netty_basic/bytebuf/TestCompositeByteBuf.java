package cn.itcast.netty_basic.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static cn.itcast.netty_basic.bytebuf.ByteBufUtil.log;

/**
 * <h2>零拷贝-CompositeByteBuf</h2>
 * <pre>
 * 1. ByteBufAllocator.DEFAULT#compositeBuffer将几个小的ByteBuf组合成一个大的ByteBuf
 * 2. 几个小的ByteBuf和大的ByteBuf底层使用同一块内存，从减少内存复制的角度上看就是“零拷贝”
 * 3. 注意：CompositeByteBuf最好与retain和release方法配套使用
 * </pre>
 */
public class TestCompositeByteBuf {

    public static void main(String[] args) {

        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 组合buf1和buf2
        // 传统方法，这种情况下会发生数据复制(不推荐)
        // ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        // buffer.writeBytes(buf1).writeBytes(buf2);
        // log(buffer);

        // 使用CompositeByteBuf来组合两个ByteBuf，底层还是使用原始的两块ByteBuf内存，不会发生数据复制(推荐)
        CompositeByteBuf buffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        // 注意：使用带bool参数的方法，会自动调整读写指针位置
        buffer.addComponents(true, buf1, buf2);
        // 避免引用计数意外减成0
        buffer.retain();
        log(buffer);
        buffer.release();
    }

}
