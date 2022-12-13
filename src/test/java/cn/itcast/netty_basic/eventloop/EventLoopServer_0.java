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
 * <h2>EventLoop处理IO事件之服务端</h2>
 * <pre>
 * 使用run模式启动
 * <p></p>
 * 一旦Client与Server建立连接，会发生如下几件事情：
 * 1) Server端的accept事件发生，Netty内置的accept事件处理器acceptor会调用ChannelInitializer#initChannel方法，添加读写事件的事件处理器
 * 2) SocketChannel会与Server端的一个EventLoop(NioEventLoopGroup或DefaultEventLoopGroup中的一个EventLoop)进行绑定，后续的事件处理(包括IO事件、其他任务、定时任务)都会使用同一个绑定的EventLoop来处理
 * </pre>
 */
@Slf4j
public class EventLoopServer_0 {

    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 这里的msg是ByteBuf类型
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(StandardCharsets.UTF_8)); // ByteBuf->String
                            }
                        });
                    }
                })
                .bind(8080);
    }

}
