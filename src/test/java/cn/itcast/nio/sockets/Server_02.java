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
 * 使用"NIO+单线程"来理解非阻塞模式
 * <br>
 * 使用run模式运行服务端
 * <br>
 * 在连接建立后和有数据时才进行日志输出，防止刷屏
 * <br>
 * 非阻塞模式下，accept方法和read方法都是非阻塞方法，在单线程环境下，各个非阻塞方法之间不会相互影响
 * <br>
 * 结论：
 * <br>
 * 1. 使用“NIO+非阻塞模式+单线程”可以处理多个客户端的连接和读写，相互之间互不影响
 * <br>
 * 2. 但是当没有连接和读写发生的时候，这个单线程会一直忙等空转，造成CPU资源浪费，需要进一步优化：{@link Server_03}
 */
@Slf4j
public class Server_02 {

    public static void main(String[] args) throws IOException {

        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建了服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 设置ServerSocketChannel为非阻塞模式，影响的是accept方法，其变为非阻塞方法
        ssc.configureBlocking(false);

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合
        List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            // 4. 建立与客户端的连接(accept), SocketChannel用来与客户端通信
//            log.debug("connecting...");
            // 此时accept方法不再是阻塞方法，线程还会继续运行不会停止，如果没有连接建立，accept方法返回null
            SocketChannel sc = ssc.accept();
//            log.debug("connected... {}", sc);
            if (sc != null) {
                log.debug("connected... {}", sc);
                // 设置SocketChannel为非阻塞模式，影响的是read方法，其变为非阻塞方法
                sc.configureBlocking(false);
                channelList.add(sc);
            }
            for (SocketChannel socketChannel : channelList) {
                // 此时read方法不再是阻塞方法, 线程还会继续运行不会停止, 如果没有读到数据, read方法返回0
                int read = socketChannel.read(buffer);
//                System.out.println("read = " + read);
                if (read > 0) {
                    // 5. 接收客户端发送的数据
                    System.out.println("read = " + read);
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("after read... {}", socketChannel);
                }
            }
        }
    }

}
