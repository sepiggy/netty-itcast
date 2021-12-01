package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.itcast.nio.c2.ByteBufferUtil.debugRead;

/**
 * 使用nio来理解非阻塞模式
 */
@Slf4j
public class Server_02 {
    public static void main(String[] args) throws IOException {

        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建了服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 设置 ServerSocketChannel 为非阻塞模式, 对应的 accept 方法不是阻塞方法

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合
        List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            // 4. 建立与客户端的连接(accept), SocketChannel 用来与客户端通信
            SocketChannel sc = ssc.accept(); // 此时 accept 方法不是阻塞方法，线程会继续运行，如果没有建立连接，sc 为 null
            if (sc != null) {
                log.debug("connected... {}", sc);
                sc.configureBlocking(false); // 设置 SocketChannel 为非阻塞模式，对应的 read 方法不是阻塞方法
                channelList.add(sc);
            }
            for (SocketChannel socketChannel : channelList) {
                int read = socketChannel.read(buffer); // 此时 read 方法不是阻塞方法, 线程会继续运行, 如果没有读到数据, read 方法返回 0
                if (read > 0) {
                    // 5. 接收客户端发送的数据
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("after read... {}", socketChannel);
                }
            }
        }
    }
}
