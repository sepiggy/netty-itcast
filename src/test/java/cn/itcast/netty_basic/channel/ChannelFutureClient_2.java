package cn.itcast.netty_basic.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <h2>方法2.使用ChannelFuture#addListener(回调对象)方法来异步处理Channel建立</h2>
 * <pre>
 * 注意：此方法下等待结果和处理结果的都是NioEventLoopGroup线程:
 * 1) 等待什么结果？等待连接建立的结果，也就是等待BootStrap#connect方法完成的结果
 * 2) 处理什么结果？处理连接建立的结果，这里就是获取到Channel对象
 * 3) 真正执行连接建立操作的也就是响应accept事件的也是NioEventLoopGroup线程
 * </pre>
 */
@Slf4j
public class ChannelFutureClient_2 {

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

        // 使用addListener(回调对象)方法异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            // 在NioEventLoopGroup线程连接建立完毕之后，会调用"operationComplete"方法
            public void operationComplete(ChannelFuture future) throws Exception {
                // 注意：以下三行代码会在NioEventLoopGroup线程中执行
                Channel channel = future.channel();
                // 22:45:09 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.EventLoopClient_3 - [id: 0x405d76b8, L:/127.0.0.1:47048 - R:localhost/127.0.0.1:8080]
                log.debug("{}", channel);
                channel.writeAndFlush("hello, world");
            }
        });
    }

}
