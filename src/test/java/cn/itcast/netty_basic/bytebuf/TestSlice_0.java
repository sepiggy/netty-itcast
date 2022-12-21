package cn.itcast.netty_basic.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static cn.itcast.netty_basic.bytebuf.ByteBufUtil.log;

/**
 * <h2>零拷贝-Slice</h2>
 * <h3>切片与原始数组底层使用同一块内存</h3>
 * <pre>
 * Slice是Netty中对零拷贝的体现之一
 * 对原始的ByteBuf进行切分成多个ByteBuf，切片后的ByteBuf并没有发生内存复制，还是使用原始ByteBuf的内存
 * 从"减少内存复制"的角度上讲发生了"零拷贝"，并不是指操作系统层面的"零拷贝"
 * 切片后的ByteBuf维护独立的read，write指针
 * </pre>
 */
public class TestSlice_0 {

    public static void main(String[] args) {

        ByteBuf origin = ByteBufAllocator.DEFAULT.buffer(10);
        origin.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(origin);

        // 在切片过程中，没有发生数据复制
        ByteBuf slice1 = origin.slice(0, 5);
        log(slice1);

        ByteBuf slice2 = origin.slice(5, 5);
        log(slice2);

        // 证明origin和两个切片底层使用同一块内存
        System.out.println("========================");
        slice1.setByte(0, 'z');
        log(slice1);
        log(origin);
    }

}
