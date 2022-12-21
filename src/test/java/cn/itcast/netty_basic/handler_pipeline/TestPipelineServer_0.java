package cn.itcast.netty_basic.handler_pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <h2>Handler和Pipeline演示的服务端</h2>
 * <h3>入站和出站处理器执行顺序</h3>
 * <pre>
 * 1) Handler分为两种：入站处理器和出站处理器
 * 2) 入站处理器一般继承 {@link ChannelInboundHandlerAdapter}
 * 3) 出站处理器一般继承 {@link ChannelOutboundHandlerAdapter}
 * 4) Netty中默认存在两个处理器head和tail，调用ChannelPipeline#addLast方法会将新处理器加入到tail处理器之前: head -> newHandler -> tail
 * 5) 入站处理器一般关心读取，一般重写"ChannelInboundHandlerAdapter#channelRead"方法
 * 6) 出站处理器一般关心写入，一般重写"ChannelOutboundHandlerAdapter#write"方法
 * 7) 入站处理器是按照加入Pipeline的顺序执行的
 * 8) 出站处理器是按照加入Pipeline的顺序相反方向执行的
 * 9) 出站处理器只有向Channel中写入数据时才会触发出站动作
 * 输出结果：
 * 13:04:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_0: 61 - 1
 * 13:04:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_0: 71 - 2
 * 13:04:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_0: 81 - 3
 * 13:04:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_0: 113 - 6
 * 13:04:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_0: 103 - 5
 * 13:04:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_0: 93 - 4
 * </pre>
 */
@Slf4j
public class TestPipelineServer_0 {

    public static void main(String[] args) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        // 1. 通过Channel拿到Pipeline
                        ChannelPipeline pipeline = ch.pipeline();

                        // 2. 添加处理器
                        // 其处理器添加调用链为：head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                        // 其处理器执行顺序为：head -> h1 -> h2 -> h3 -> tail -> h6 -> h5 -> h4
                        pipeline.addLast("h1", new H1InboundHandler());
                        pipeline.addLast("h2", new H2InboundHandler());
                        pipeline.addLast("h3", new H3InboundHandler());
                        pipeline.addLast("h4", new H4OutboundHandler());
                        pipeline.addLast("h5", new H5OutboundHandler());
                        pipeline.addLast("h6", new H6OutboundHandler());
                    }
                })
                .bind(8080);
    }

    static final class H1InboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("1");
            super.channelRead(ctx, msg);
        }

    }

    static final class H2InboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("2");
            super.channelRead(ctx, msg);
        }

    }

    static final class H3InboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("3");
            super.channelRead(ctx, msg);
            // 这个写入操作只是为了触发出站处理器h4,h5,h6
            ctx.channel().writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes(StandardCharsets.UTF_8)));
        }

    }

    static final class H4OutboundHandler extends ChannelOutboundHandlerAdapter {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("4");
            super.write(ctx, msg, promise);
        }

    }

    static final class H5OutboundHandler extends ChannelOutboundHandlerAdapter {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("5");
            super.write(ctx, msg, promise);
        }

    }

    static final class H6OutboundHandler extends ChannelOutboundHandlerAdapter {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("6");
            super.write(ctx, msg, promise);
        }

    }

}
