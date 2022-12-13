package cn.itcast.netty_basic.c3;

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
 * 完整版
 * 客户端由控制台接收用户输入
 * 直到向控制台输入 q
 * 表示退出
 *
 * 正确善后方法演示:
 * 通过 closeFuture 对象进行善后：
 * 1) 同步处理关闭: closeFuture#sync
 * 2) 异步处理关闭: closeFuture#addListener
 *
 * 优雅地在 Channel 关闭之后退出客户端: NioEventLoopGroup#shutdownGracefully
 */
@Slf4j
public class CloseFutureClient_3 {
    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup();

        // 一个 ChannelFuture 处理连接建立
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
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

        new Thread(()-> {
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

        // 另一个 ChannelFuture 处理连接关闭
        ChannelFuture closeFuture = channel.closeFuture();

        // 异步处理关闭
        closeFuture.addListener((ChannelFutureListener) future -> {
            log.debug("处理关闭之后的操作");
            group.shutdownGracefully();
        });
    }
}
