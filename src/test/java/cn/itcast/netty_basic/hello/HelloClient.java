package cn.itcast.netty_basic.hello;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * HelloWorld客户端
 * <br>
 * 开发一个简单的服务器端和客戶端:
 * <br>
 * 1) 客戶端向服务器端发送 hello, world
 * <br>
 * 2) 服务器仅接收，不返回
 */
public class HelloClient {

    public static void main(String[] args) throws InterruptedException {
        // 1. Bootstrap: 客户端启动器，负责组装netty组件，启动客户端
        new Bootstrap()
                // 2. 添加EventLoop
                .group(new NioEventLoopGroup())
                // 3. 选择客户端的SocketChannel实现(对原生的SocketChannel进行封装)
                .channel(NioSocketChannel.class)
                // 4. 添加读写事件发生时的事件处理器（Handler）
                .handler(
                        // 5. 添加"Channel初始化器"
                        //    注意：ChannelInitializer(Channel初始化器)是一种对Channel进行初始化的特殊的事件处理器(Handler)
                        //    注意：ChannelInitializer会在连接建立后(即accept事件发生后)执行initChannel方法
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override // 在连接建立后被调用
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                // 注意：在Netty中收发数据都会走Handler
                                ch.pipeline().addLast(new StringEncoder()); // 编码器: String->ByteBuf
                            }
                        })
                // 6. 连接到服务器
                .connect(new InetSocketAddress("localhost", 8080))
                .sync() // 阻塞方法，直到连接建立
                .channel() // 代表服务器和客户端通信的Channel
                // 7. 向服务器端发送数据
                .writeAndFlush("hello, world"); // 向Channel写数据(发送数据到服务器端)
    }

}
