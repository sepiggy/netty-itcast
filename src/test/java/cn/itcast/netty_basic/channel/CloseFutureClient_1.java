package cn.itcast.netty_basic.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
 * <h2>方法1.使用CloseFuture#sync方法同步处理Channel关闭</h2>
 * <pre>
 * 注意：此方法下等待结果和处理结果的都是main线程:
 * 1) 等待什么结果？等待连接关闭的结果，也就是等待Channel#closeFuture方法完成的结果
 * 2) 处理什么结果？处理连接关闭结果，这里就是进行一些善后处理工作
 * 3) 真正执行连接关闭操作的也就是响应close事件的是NioEventLoopGroup线程
 * 4) Channel关闭后相应的可以调用NioEventLoopGroup#shutdownGracefully方法关闭事件循环组
 * </pre>
 */
@Slf4j
public class CloseFutureClient_1 {

    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

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
                    channel.close(); // close是异步操作，交给其它线程去执行关闭动作
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 获取CloseFuture对象
        // NioEventLoopGroup线程完成Close操作:
        // 22:40:32 [DEBUG] [nioEventLoopGroup-2-1] i.n.h.l.LoggingHandler: 147 - [id: 0xdb19c949, L:/127.0.0.1:50006 - R:localhost/127.0.0.1:8080] CLOSE
        ChannelFuture closeFuture = channel.closeFuture();

        // 同步处理关闭
        // 注意：以下三行代码均在main线程中执行
        // 22:37:28 [DEBUG] [main] c.i.n.c.CloseFutureClient_1: 67 - waiting close...
        log.debug("waiting close...");
        closeFuture.sync(); // 同步处理关闭
        // 22:37:31 [DEBUG] [main] c.i.n.c.CloseFutureClient_1: 70 - 处理关闭之后的操作
        log.debug("处理关闭之后的操作");
        // 优雅关闭事件线程组，整个客户端JVM进程正常退出
        nioEventLoopGroup.shutdownGracefully();
    }

}
