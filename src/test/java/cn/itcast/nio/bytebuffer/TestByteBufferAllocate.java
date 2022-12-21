package cn.itcast.nio.bytebuffer;

import java.nio.ByteBuffer;

/**
 * ByteBuffer#allocate方法
 */
public class TestByteBufferAllocate {

    public static void main(String[] args) {
        System.out.println(ByteBuffer.allocate(16).getClass()); // class java.nio.HeapByteBuffer
        System.out.println(ByteBuffer.allocateDirect(16).getClass()); // class java.nio.DirectByteBuffer
        /*
        class java.nio.HeapByteBuffer    - 使用 java 堆内存，读写效率较低，受到 GC 的影响，分配的效率高
        class java.nio.DirectByteBuffer  - 使用系统内存，读写效率高（少一次拷贝），不会受 GC 影响，分配的效率低
         */
    }

}
