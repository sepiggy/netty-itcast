package cn.itcast.nio.bytebuffer;

import java.nio.ByteBuffer;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * ByteBuffer与读取相关的方法
 */
public class TestByteBufferRead {

    public static void main(String[] args) {

        ByteBuffer buffer = ByteBuffer.allocate(10); // position:0, limit:10, capacity:10
        debugAll(buffer);

        buffer.put(new byte[]{'a', 'b', 'c', 'd'}); // position:4, limit:10, capacity:10
        debugAll(buffer);

        // 切换到读模式
        buffer.flip(); // position:0, limit:4, capacity:10
        debugAll(buffer);

        // 从buffer中读取数据到byte数组中
        buffer.get(new byte[4]); // position:4, limit:4, capacity:10
        debugAll(buffer);

        // rewind从头开始读
        buffer.rewind(); // position:0, limit:4, capacity:10
        debugAll(buffer);

        System.out.println((char) buffer.get()); // position:1, limit:4, capacity:10
        debugAll(buffer);

        // mark & reset (rewind 增强)
        // mark: 做一个标记，记录position位置
        // reset: 是将position重置到mark的位置
        System.out.println((char) buffer.get());     // position:2, limit:4, capacity:10
        buffer.mark();                               // position:2, limit:4, capacity:10, mark:2
        System.out.println((char) buffer.get());     // position:3, limit:4, capacity:10, mark:2
        debugAll(buffer);
        buffer.reset();                              // position:2, limit:4, capacity:10, mark:2
        debugAll(buffer);
        System.out.println((char) buffer.get());     // position:3, limit:4, capacity:10, mark:2
        debugAll(buffer);

        // get(i)不会改变position指针的位置
        System.out.println((char) buffer.get(0));    // position:3, limit:4, capacity:10, mark:2
        debugAll(buffer);
    }

}
