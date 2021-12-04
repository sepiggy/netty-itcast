package cn.itcast.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static cn.itcast.netty.c4.TestByteBuf.log;

/**
 * ATTN CompositeByteBuf 最好与 retain 和 release 方法配套使用
 */
public class TestCompositeByteBuf {

    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});


        // 传统方法，这种情况下会发生数据复制（不推荐）
        /*ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(buf1).writeBytes(buf2);
        log(buffer);*/

        // 使用 CompositeByteBuf 来组合两个 ByteBuf，底层还是使用原始的两块 ByteBuf内存，不会发生数据复制 （推荐）
        CompositeByteBuf buffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        buffer.addComponents(true, buf1, buf2); // 调用带 bool 参数的方法，来改变读写指针
        buffer.retain();
        log(buffer);
        buffer.release();
    }

}
