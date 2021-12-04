package cn.itcast.netty.c3;

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
 * 法1. 使用 ChannelFuture#sync 方法来同步主线程和 NioEventLoop 线程
 * ATTN 此种情况下等待连接建立结果和处理结果的是 main 线程
 */
@Slf4j
public class EventLoopClient_2 {

    public static void main(String[] args) throws InterruptedException {

        // ATTN 带有 Future、Promise 名称的类型通常都是和异步方法配套使用处理结果的
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // ATTN connect 是异步非阻塞方法
                // main 线程发起了调用
                // 真正执行连接操作的是另一个线程 （Nio线程)
                // 这里的连接操作是比较耗时的
                // 在此期间是获取不到 Channel 对象的
                // 因此需要调用 ChannelFuture 的 sync 方法
                .connect(new InetSocketAddress("localhost", 8080));

        // 使用 sync 方法来同步处理结果
        channelFuture.sync(); // 阻塞当前线程在此，直到 NioEventLoop 线程连接建立完毕

        // 以下三行代码会在 main 线程执行
        // 无阻塞向下执行获取 Channel 对象
        Channel channel = channelFuture.channel();
        // 22:35:35 [DEBUG] [main] c.i.n.c.EventLoopClient_2 - [id: 0x636c89a4, L:/127.0.0.1:47016 - R:localhost/127.0.0.1:8080]
        log.debug("{}", channel);
        channel.writeAndFlush("hello, world");
    }

}
