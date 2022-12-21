package cn.itcast.netty_basic.handler_pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * <h2>Handler和Pipeline演示的服务端</h2>
 * <h3>关于入站处理器的说明</h3>
 * <pre>
 * 1) "super.channelRead(ctx,msg)"方法会调用"ctx.fireChannelRead(msg)"方法将执行权转交给下一个入站Handler，并将本个Handler的数据处理结果传递给下一个Handler
 * 2) 将入站处理器(Inbound-Handler)分成多个挂载在Pipeline上是为了数据的多步加工：由原始的ByteBuf类型一步一步转化为我们需要的类型
 * 3) 为了保持入站处理器调用链的完整性：必须调用"super.channelRead(ctx, msg);"方法
 * 运行结果：
 * 13:27:30 [DEBUG] [nioEventLoopGroup-2-3] c.i.n.h.TestPipelineServer_1: 51 - 1
 * 13:27:30 [DEBUG] [nioEventLoopGroup-2-3] c.i.n.h.TestPipelineServer_1: 64 - 2
 * 13:27:30 [DEBUG] [nioEventLoopGroup-2-3] c.i.n.h.TestPipelineServer_1: 78 - 3, 结果: TestPipelineServer_1.Student(name=张三), class: class cn.itcast.netty_basic.handler_pipeline.TestPipelineServer_1$Student
 * 13:27:30 [DEBUG] [nioEventLoopGroup-2-3] c.i.n.h.TestPipelineServer_1: 110 - 6
 * 13:27:30 [DEBUG] [nioEventLoopGroup-2-3] c.i.n.h.TestPipelineServer_1: 100 - 5
 * 13:27:30 [DEBUG] [nioEventLoopGroup-2-3] c.i.n.h.TestPipelineServer_1: 90 - 4
 * </pre>
 */
@Slf4j
public class TestPipelineServer_1 {

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
                        pipeline.addLast("h3", new H3InboundHandler());
                        pipeline.addLast("h4", new H4OutboundHandler());
                        pipeline.addLast("h5", new H5OutboundHandler());
                        pipeline.addLast("h6", new H6OutboundHandler());
                    }
                })
                .bind(8080);
    }

    static final class H1InboundHandler extends ChannelInboundHandlerAdapter {

        // 数据加工：原始的ByteBuf -> String
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("1");
            ByteBuf buf = (ByteBuf) msg;
            String name = buf.toString(StandardCharsets.UTF_8);
            super.channelRead(ctx, name); // 将msg传给下一个Handler
        }

    }

    static final class H2InboundHandler extends ChannelInboundHandlerAdapter {

        // 数据加工：String -> Student
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
            log.debug("2");
            Student student = new Student(name.toString());
            // 注意：这里将数据传递给下个Handler，如果不调用，调用链会断开
            // 或者调用 "ctx.fireChannelRead(student);" 方法
            super.channelRead(ctx, student);
        }

    }

    static final class H3InboundHandler extends ChannelInboundHandlerAdapter {

        // 拿到Student类型数据
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("3, 结果: {}, class: {}", msg, msg.getClass());
            // 可以不调用 super.channelRead 方法，因为是最后一个入站处理器
            // 开始写出操作
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

    @Data
    @AllArgsConstructor
    static final class Student {

        private String name;

    }

}
