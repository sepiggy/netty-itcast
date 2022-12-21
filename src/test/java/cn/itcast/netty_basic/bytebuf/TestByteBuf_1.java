package cn.itcast.netty_basic.bytebuf;//package cn.itcast.netty_basic.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * <h2>ByteBuf内存模式：直接内容或者堆内存</h2>
 * <pre>
 * 1) "ByteBufAllocator.DEFAULT#buffer();"          生成的ByteBuf默认使用直接内存
 * 2) "ByteBufAllocator.DEFAULT#heapBuffer();"      明确指定ByteBuf类型为堆内存
 * 3) "ByteBufAllocator.DEFAULT#directBuffer();"    明确指定ByteBuf类型为直接内存
 * </pre>
 */
public class TestByteBuf_1 {

    public static void main(String[] args) {

        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        System.out.println("buf1 = " + buf1);

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.heapBuffer();
        System.out.println("buf2 = " + buf2);

        ByteBuf buf3 = ByteBufAllocator.DEFAULT.directBuffer();
        System.out.println("buf3 = " + buf3);
    }

}
