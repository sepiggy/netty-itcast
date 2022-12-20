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
 * <h2>ChannelFuture</h2>
 * 参考 {@link ChannelFuture} 的JavaDoc
 * <pre>
 * 1. BootStrap#connect方法是"异步非阻塞"方法.
 *    所谓异步非阻塞方法是指调用connect方法的main线程不获取其执行结果，只是发起了调用.
 *    真正执行connect操作的是另一个线程(NioEventLoopGroup中的一个线程)，因此main线程可以继续向下执行.
 *    而连接操作相对比较耗时，若连接没有建立好，main线程继续向下执行”channelFuture#channel"这一行是拿不到Channel的.
 * 2. 发起连接请求的是main线程，真正建立连接获取Channel的是NioEventLoopGroup中的一个线程
 * 3. 因此需要保证在获取Channel的时候，连接已经成功建立，有两种方法：
 *    1) 使用ChannelFuture#sync方法同步处理Channel建立 {@link ChannelFutureClient_1}
 *    2) 使用ChannelFuture#addListener方法异步处理Channel建立 {@link ChannelFutureClient_2}
 * </pre>
 */
@Slf4j
public class ChannelFutureClient_0 {

    public static void main(String[] args) throws InterruptedException {

        // 2. 带有 Future，Promise 的类型都是和异步方法配套使用，用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 注意：connect是异步非阻塞方法
                // 1. 连接到服务器
                // 异步非阻塞, main线程发起了调用，真正执行connect是NioEventLoop线程
                .connect(new InetSocketAddress("localhost", 8080));

        // 无阻塞向下执行获取Channel对象
        Channel channel = channelFuture.channel();

        log.debug("{}", channel);

        channel.writeAndFlush("hello, world");
    }

}
