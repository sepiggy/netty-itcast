package cn.itcast.netty_basic.eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * <h2>EventLoop演示之客户端</h2>
 * <pre>
 * 使用debug模式启动
 * <p></p>
 * 注意：因为Netty的客户端是多线程的，其执行Channel写操作的线程并不是主线程，因此打断点时要使用IDEA的Thread模式，不会阻塞所有线程
 * 注意：在debug模式evaluate如下表达式'channel.writeAndFlush("1")' (注意必须是字符串"1"，不能是数字1)
 * </pre>
 */
public class EventLoopClient {

    public static void main(String[] args) throws InterruptedException {
        Channel channel = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080))
                .sync()
                .channel();

        System.out.println(channel);

        System.out.println(""); // 注意：这里要使用Thread模式断点
    }

}
