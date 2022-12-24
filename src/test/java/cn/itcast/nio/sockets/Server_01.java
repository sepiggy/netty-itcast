package cn.itcast.nio.sockets;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugRead;

/**
 * 使用"NIO+单线程"来理解阻塞模式 (服务端)
 * <br>
 * 使用Run模式运行服务端
 * <br>
 * 阻塞模式下，accept方法和read方法都是阻塞方法，且在单线程环境下，多个阻塞方法会相互影响
 * <br>
 * 结论：使用“NIO+单线程+阻塞模式”不可以处理多个客户端的连接和读写，相互之间会有影响，需要进一步优化：{@link Server_02}
 */
@Slf4j
public class Server_01 {

    public static void main(String[] args) throws IOException {

        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建了服务器 (ServerSocketChannel)
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合
        List<SocketChannel> channelList = new ArrayList<>();

        // 不断接收连接，将连接SocketChannel放入连接集合
        while (true) {
            // 4. 建立与客户端的连接(accept), SocketChannel用来与客户端通信
            log.debug("connecting...");
            // SocketChannel是服务端与客户端进行读写操作的通道
            SocketChannel sc = ssc.accept(); // accept是阻塞方法，线程停止运行，等待新连接建立

//            sc.configureBlocking(true);
//            System.out.println("sc.isBlocking() = " + sc.isBlocking());

            log.debug("connected... {}", sc);
            channelList.add(sc);
            for (SocketChannel socketChannel : channelList) {
                log.debug("before read... {}", socketChannel);
                // 从SocketChannel读取数据写入缓冲区
                socketChannel.read(buffer); // read是阻塞方法, 线程停止运行，等待读取内容
                // 5. 接收客户端发送的数据
                buffer.flip(); // 切换为读模式
                debugRead(buffer);
                buffer.clear(); // 切换为写模式
                log.debug("after read... {}", socketChannel);
            }
        }
    }

}
