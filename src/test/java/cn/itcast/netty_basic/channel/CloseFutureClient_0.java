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
 * <h2>错误善后方法演示1</h2>
 * <pre>
 * 56行和63行都是错误的善后处理方式
 * </pre>
 * <pre>
 * 需求：
 * 客户端由控制台接收用户输入并将其发送给服务端
 * 直到用户向控制台输入q表示退出
 * 如何实现这个需求，优雅地关闭Channel并执行相应的善后代码?
 * </pre>
 * <pre>
 * ChannelFuture#close同BootStrapServer#connect方法也是一个异步非阻塞方法，
 * 因此也是需要保证某些善后操作在close方法执行后才能执行，这里同 {@link ChannelFutureClient_0} 也是两种方式(因为都是ChannelFuture对象):
 * 1) 使用CloseFuture#sync方法同步处理Channel关闭，{@link CloseFutureClient_1}
 * 2) 使用CloseFuture#addListener方法异步处理Channel关闭，{@link CloseFutureClient_2}
 *
 * </pre>
 */
@Slf4j
public class CloseFutureClient_0 {

    public static void main(String[] args) throws InterruptedException {

        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG)); // Netty内置的Handler, 需要同步配置logback.xml
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));

        Channel channel = channelFuture.sync().channel(); // 使用sync方法等待连接建立完毕

        log.debug("{}", channel);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close(); // close是异步操作，交给其它线程去执行关闭动作，这行是在NioEventLoopGroup线程中执行
                    log.debug("处理关闭之后的操作"); // 这里也是错误写法，并没有等连接关闭后执行善后代码，这行是在input线程中执行，与上面这行代码谁先谁后执行并不确定
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

//        log.debug("处理关闭之后的操作"); // 这里是错误写法，并没有等连接关闭后执行善后代码
    }

}
