package cn.itcast.nio.bytebuffer;

import java.nio.ByteBuffer;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * 利用调试工具类测试ByteBuffer的读写
 * <p></p>
 * ByteBuffer写入的方式：
 * 1) FileChannel#read
 * 2) ByteBuffer#put
 */
public class TestByteBufferReadWrite {

    public static void main(String[] args) {

        ByteBuffer buffer = ByteBuffer.allocate(10); // position=0, limit=10, capacity=10
        buffer.put((byte) 0x61); // 写入字符a: position=1, limit=10, capacity=10
        debugAll(buffer);

        buffer.put(new byte[]{0x62, 0x63, 0x64}); // 写入字符b,c,d: position=4, limit=10, capacity=10
        debugAll(buffer);
        // System.out.println(buffer.get()); // 0, 不切换至读模式强行读取

        buffer.flip(); // position=0, limit=4, capacity=10
        System.out.println(buffer.get()); // position=1, limit=4, capacity=10
        debugAll(buffer);

        buffer.compact(); // position=3, limit=10, capacity=10
        debugAll(buffer);

        buffer.put(new byte[]{0x65, 0x66}); // position=5, limit=10, capacity=10
        debugAll(buffer);
    }

}
