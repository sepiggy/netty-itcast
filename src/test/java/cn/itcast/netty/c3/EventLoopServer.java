package cn.itcast.netty.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 对 Server 端进行功能细分
 * 无论是哪种 EventLoopGroup, 只要绑定一次，后面就只会用这个线程处理对应的事件或者 Handler
 * NioEventLoopGroup: 处理 Accept, Read, Write 事件
 * DefaultEventLoopGroup: 处理用时较长的 Handler
 */
@Slf4j
public class EventLoopServer {

    public static void main(String[] args) {
        // 细分2：创建一个独立的 EventLoopGroup
        // 用来处理耗时较长的操作，避免阻塞 IO 线程
        // 可以单独声明一个 EventLoopGroup 单独处理一个耗时较长的 Handler,
        // 这样做可以避免阻塞 IO 线程
        EventLoopGroup group = new DefaultEventLoopGroup();
        new ServerBootstrap()
                // boss 和 worker
                // 细分1：
                // boss 只负责 ServerSocketChannel 上 accept 事件
                // worker 只负责 socketChannel 上的读写
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter() {
                            @Override                                         // ByteBuf
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(Charset.defaultCharset()));
                                ctx.fireChannelRead(msg); // 让消息传递给下一个handler
                            }
                        }).addLast(group, "handler2", new ChannelInboundHandlerAdapter() {
                            // 处理这个 handler 用的线程不是 worker 线程，而是上面新建的事件循环组
                            @Override                                         // ByteBuf
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .bind(8080);
    }

}
