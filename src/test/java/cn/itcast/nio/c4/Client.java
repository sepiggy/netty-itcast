package cn.itcast.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * NIO客户端
 * <p></p>
 * 需要使用Debug模式运行
 * <p></p>
 * 在Debug模式下运行表达式: sc.write(StandardCharsets.UTF_8.encode("abc"));
 */
public class Client {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        System.out.println("sc.isBlocking() = " + sc.isBlocking());
        sc.connect(new InetSocketAddress("localhost", 8080));
        System.out.println("waiting...");
    }

}
