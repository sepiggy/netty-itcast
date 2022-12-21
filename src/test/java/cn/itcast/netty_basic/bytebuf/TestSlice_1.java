package cn.itcast.netty_basic.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static cn.itcast.netty_basic.bytebuf.ByteBufUtil.log;

/**
 * <h2>零拷贝-Slice</h2>
 * <pre>
 * 1. ByteBuf#slice方法生成的ByteBuf其最大容量会有限制，不能随意扩容
 * 2. 对原始的ByteBuf进行release操作会影响切片ByteBuf
 *    注意：最佳实践：ByteBuf#slice最好与retain方法配套使用
 * </pre>
 */
public class TestSlice_1 {

    public static void main(String[] args) {

        ByteBuf origin = ByteBufAllocator.DEFAULT.buffer(10);
        origin.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(origin);

        ByteBuf slice1 = origin.slice(0, 5);
        // 注意：保证原始的ByteBuf调用release方法之后，这块内存也不会被释放掉
        slice1.retain();

        ByteBuf slice2 = origin.slice(5, 5);
        slice2.retain();

        System.out.println("==================");
        // 释放原有ByteBuf内存
        origin.release();
        log(slice1);

        // 注意：用完切片自己release
        slice1.release();
        slice2.release();
    }

}
