package cn.itcast.netty_basic.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <h2>方法1.使用ChannelFuture#sync方法同步处理Channel建立</h2>
 * <pre>
 * 注意：此方法下等待结果和处理结果的都是main线程:
 * 1) 等待什么结果？等待连接建立的结果，也就是等待BootStrap#connect方法完成的结果
 * 2) 处理什么结果？处理连接建立的结果，这里就是获取到Channel对象
 * 3) 真正执行连接建立操作的也就是响应accept事件的是NioEventLoopGroup线程
 * </pre>
 */
@Slf4j
public class ChannelFutureClient_1 {

    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));

        // ChannelFuture#sync方法是"同步阻塞"方法，使用sync方法来同步处理结果
        channelFuture.sync(); // main线程阻塞在此，直到NioEventLoop线程连接建立完毕

        // 注意：以下三行代码会在main线程执行
        Channel channel = channelFuture.channel();
        // 22:35:35 [DEBUG] [main] c.i.n.c.EventLoopClient_2 - [id: 0x636c89a4, L:/127.0.0.1:47016 - R:localhost/127.0.0.1:8080]
        log.debug("{}", channel);
        channel.writeAndFlush("hello, world");
    }

}
