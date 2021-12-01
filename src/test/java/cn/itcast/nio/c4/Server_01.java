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
 * 使用nio来理解阻塞模式, 单线程
 */
@Slf4j
public class Server_01 {
    public static void main(String[] args) throws IOException {

        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建了服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合
        List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            // 4. 建立与客户端的连接(accept), SocketChannel 用来与客户端通信
            log.debug("connecting...");
            SocketChannel sc = ssc.accept(); // accept 是阻塞方法，线程停止运行，等待新连接建立
            log.debug("connected... {}", sc);
            channelList.add(sc);
            for (SocketChannel socketChannel : channelList) {
                log.debug("before read... {}", socketChannel);
                socketChannel.read(buffer); // read 是阻塞方法, 线程停止运行，等待读取内容
                // 5. 接收客户端发送的数据
                buffer.flip(); // 切换为读模式
                debugRead(buffer);
                buffer.clear(); // 切换为写模式
                log.debug("after read... {}", socketChannel);
            }
        }
    }
}
