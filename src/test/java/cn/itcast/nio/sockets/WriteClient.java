package cn.itcast.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 演示可写事件客户端 (大量数据一次写不完的情况)
 */
public class WriteClient {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        //  接收数据
        int count = 0;
        while (true) {
            // 每循环一次就分配一个ByteBuffer
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            // 从SocketChannel读取数据写入ByteBuffer
            count += sc.read(buffer);
            System.out.println("count = " + count);
            buffer.clear();
        }
    }

}
