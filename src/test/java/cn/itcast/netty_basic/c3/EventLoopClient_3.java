package cn.itcast.netty_basic.c3;

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
 * 法2. 使用 ChannelFuture#addListener(回调对象) 方法来异步处理结果
 * ATTN 此种情况下等待连接建立结果和处理结果的不是 main 线程
 */
@Slf4j
public class EventLoopClient_3 {

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

        // 使用 addListener(回调对象) 方法异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            // 在 NioEventLoopGroup 线程连接建立完毕之后，会调用 operationComplete 方法
            // 以下三行代码会在 NioEventLoopGroup 线程执行
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                // 22:45:09 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.EventLoopClient_3 - [id: 0x405d76b8, L:/127.0.0.1:47048 - R:localhost/127.0.0.1:8080]
                log.debug("{}", channel);
                channel.writeAndFlush("hello, world");
            }
        });
    }

}
