package cn.itcast.netty_basic.c3;

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
 * ChannelFuture
 */
@Slf4j
public class EventLoopClient_1 {

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
                // ATTN connect 是异步非阻塞方法
                // main 线程发起了调用
                // 真正执行连接操作的是另一个线程 （Nio线程)
                // 这里的连接操作是比较耗时的
                // 在此期间是获取不到 Channel 对象的
                // 因此需要调用 ChannelFuture 的 sync 方法
                .connect(new InetSocketAddress("localhost", 8080));

        channelFuture.sync();

        // 无阻塞向下执行获取 Channel 对象
        Channel channel = channelFuture.channel();
        log.debug("{}", channel);

        channel.writeAndFlush("hello, world");
    }

}
