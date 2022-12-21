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
 * <h3>关于出站处理器的说明</h3>
 * <pre>
 * 1) "ChannelHandlerContext#writeAndFlush"与"Channel#writeAndFlush"的区别：
 *    前者是从当前Handler节点向前找出站处理器，而后者是从tail节点向前找出站处理器
 * 运行结果：
 * 13:51:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_2: 49 - 1
 * 13:51:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_2: 59 - 2
 * 13:51:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_2: 69 - 3
 * 13:51:19 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.h.TestPipelineServer_2: 80 - 4
 * </pre>
 */
@Slf4j
public class TestPipelineServer_2 {

    public static void main(String[] args) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast("h1", new H1InboundHandler());
                        pipeline.addLast("h2", new H2InboundHandler());
                        pipeline.addLast("h4", new H4OutboundHandler());
                        pipeline.addLast("h3", new H3InboundHandler());
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
//            ctx.channel().writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes(StandardCharsets.UTF_8)));
            ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes(StandardCharsets.UTF_8)));
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
