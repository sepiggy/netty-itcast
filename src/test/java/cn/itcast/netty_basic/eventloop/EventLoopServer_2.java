package cn.itcast.netty_basic.eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <h2>耗时较长的操作可以创建专门的EventLoopGroup</h2>
 * <h3>(在{@link EventLoopServer_0}的基础上)</h3>
 * <pre>
 * 使用run模式启动
 * <p></p>
 * 可以单独声明一个"EventLoopGroup"用来处理耗时较长的Handler，避免阻塞其它Client端的读写操作(IO事件)的执行，这种类型的EventLoopGroup一般可以使用 {@link DefaultEventLoop} (恰好不能处理IO事件)
 * </pre>
 */
@Slf4j
public class EventLoopServer_2 {

    public static void main(String[] args) {

        // 单独声明的EventLoopGroup
        EventLoopGroup handler2Group = new DefaultEventLoopGroup();

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter() {

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        String s = buf.toString(StandardCharsets.UTF_8);
                                        log.debug(s);
                                        ctx.fireChannelRead(s);
                                    }
                                })
                                // 注意：addLast参数
                                // 注意：第一个参数：事件循环组
                                // 注意：第二个参数：Handler名称
                                // 注意：第三个参数：Handler的Adapter
                                .addLast(handler2Group, "handler2", new ChannelInboundHandlerAdapter() {
                                    // 处理handler2的EventLoopGroup不是Server端初始化时传入的EventLoopGroup，而是专属的EventLoopGroup
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        // 若单独指定handler2的EventLoopGroup, 则下面这句不会阻塞处理IO事件的线程
                                        // Thread.sleep(Integer.MAX_VALUE);
                                        String s = (String) msg;  // 拿到上一个Handler传递过来的msg
                                        log.debug(s);
                                    }
                                });
                    }
                })
                .bind(8080);
    }

}
