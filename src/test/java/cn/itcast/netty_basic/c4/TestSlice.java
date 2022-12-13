package cn.itcast.netty_basic.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static cn.itcast.netty_basic.c4.TestByteBuf.log;

/**
 * Slice 是 Netty 中对零拷贝的体现之一
 * ATTN slice 方法最好与 retain 和 release 方法配套使用
 */
public class TestSlice {

    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(buf);

        // 在切片过程中，没有发生数据复制
        ByteBuf f1 = buf.slice(0, 5);
        f1.retain(); // 保证原始的 ByteBuf 调用 release 方法之后，这块内存也不会被释放掉
        // 'a','b','c','d','e', 'x'
        ByteBuf f2 = buf.slice(5, 5);
        f2.retain();
        log(f1);
        log(f2);

        System.out.println("释放原有 byteBuf 内存");
        buf.release();
        log(f1);

        f1.release();
        f2.release();
        /*System.out.println("========================");
        f1.setByte(0, 'b');
        log(f1);
        log(buf);*/
    }

}
