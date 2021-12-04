package cn.itcast.netty.c3;

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
 * 客户端由控制台接收用户输入
 * 直到向控制台输入 q
 * 表示退出
 *
 * 正确善后方法演示:
 * 通过 closeFuture 对象进行善后：
 * 1) 同步处理关闭: closeFuture#sync
 * 2) 异步处理关闭: closeFuture#addListener
 */
@Slf4j
public class CloseFutureClient_2 {
    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));

        Channel channel = channelFuture.sync().channel(); // 使用 sync 方法等待连接建立完毕

        log.debug("{}", channel);

        new Thread(()-> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close(); // close 是异步操作，交给其它线程去执行关闭动作
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 获取 closeFuture 对象：1) 同步处理关闭；2) 异步处理关闭
        ChannelFuture closeFuture = channel.closeFuture();

        // 同步处理关闭
        // ATTN 以下代码在 main 线程中执行
     /*   System.out.println("waiting close...");
        closeFuture.sync(); // 同步处理关闭
        // 23:13:53 [DEBUG] [main] c.i.n.c.CloseFutureClient_2 - 处理关闭之后的操作
        log.debug("处理关闭之后的操作");*/

        // 异步处理关闭
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // ATTN 以下代码在 NioEventLoopGroup 线程中执行
                // 23:14:59 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.CloseFutureClient_2 - 处理关闭之后的操作
                log.debug("处理关闭之后的操作");
            }
        });
    }
}
