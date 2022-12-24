package cn.itcast.nio.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * NIO客户端
 * <br>
 * 需要使用Debug模式运行：
 * <br>
 * 1) 在Debug模式下运行表达式: sc.write(StandardCharsets.UTF_8.encode("abc")); 模拟向Server端发送数据
 * <br>
 * 2) 在Debug模式下运行表达式: sc.close(); 模拟正常断开
 * <br>
 * 3) 在Debug模式下运行表达式: sc.write(StandardCharsets.UTF_8.encode("hello\nworld\n")); 复现消息边界问题 {@link Server_03}
 * <br>
 * 4) 在Debug模式下运行表达式: sc.write(StandardCharsets.UTF_8.encode("0123456789abcdefghijkl\n")); 复现服务端发送的消息超过ByteBuffer的容量 {@link Server_04}
 */
public class Client {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        System.out.println("waiting...");
    }

}
