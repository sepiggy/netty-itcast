package cn.itcast.netty_basic.eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <h2>Server端的EventLoop细分为Boss和Worker</h2>
 * <h3>(在{@link EventLoopServer_0}的基础上)</h3>
 * <pre>
 * 使用run模式启动
 * <p></p>
 * 使用"ServerBootstrap#group(EventLoopGroup parentGroup, EventLoopGroup childGroup)"方法将Server端的EventLoopGroup进一步细分为两组EventLoopGroup即Boss和Worker:
 *  1)Boss只负责处理ServerSocketChannel上的accept事件
 *  2)Worker只负责处理SocketChannel上的read和write事件
 * </pre>
 */
@Slf4j
public class EventLoopServer_1 {

    public static void main(String[] args) {
        new ServerBootstrap()
                // 细分为Boss和Worker
                .group(new NioEventLoopGroup(), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .bind(8080);
    }

}
