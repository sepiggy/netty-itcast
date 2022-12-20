package cn.itcast.netty_basic.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * <h2>方法2.使用CloseFuture#addListener方法异步处理关闭</h2>
 * <pre>
 *  注意：此方法下等待结果和处理结果的都是NioEventLoopGroup线程:
 *  1) 等待什么结果？等待连接关闭的结果，也就是等待Channel#closeFuture方法完成的结果
 *  2) 处理什么结果？处理连接关闭结果，这里就是进行一些善后处理工作
 *  3) 真正执行连接关闭操作的也就是响应close事件的是NioEventLoopGroup线程
 *  4) Channel关闭后相应的可以调用NioEventLoopGroup#shutdownGracefully方法关闭事件循环组
 *  </pre>
 */
@Slf4j
public class CloseFutureClient_2 {

    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

        // 一个ChannelFuture处理连接建立
        ChannelFuture channelFuture = new Bootstrap()
                .group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));

        Channel channel = channelFuture.sync().channel();

        log.debug("{}", channel);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 另一个ChannelFuture处理连接关闭
        // NioEventLoopGroup线程完成Close操作:
        // 22:37:31 [DEBUG] [nioEventLoopGroup-2-1] i.n.h.l.LoggingHandler: 147 - [id: 0x7f11f556, L:/127.0.0.1:36672 - R:localhost/127.0.0.1:8080] CLOSE
        ChannelFuture closeFuture = channel.closeFuture();

        // 异步处理关闭
        closeFuture.addListener((ChannelFutureListener) future -> {
            // 22:36:25 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.CloseFutureClient_2: 69 - waiting close...
            log.debug("waiting close...");
            // 22:36:25 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.CloseFutureClient_2: 70 - 处理关闭之后的操作
            log.debug("处理关闭之后的操作");
            // 优雅关闭事件线程组，整个客户端JVM进程正常退出
            nioEventLoopGroup.shutdownGracefully();
        });
    }

}
