package cn.itcast.netty_basic.future_promise;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 演示 Pipeline
 * ChannelInboundHandlerAdapter: 入站处理器; 入站是按照加入 Pipeline 的顺序执行的
 * ChannelOutboundHandlerAdapter: 出站处理器，只有向 Channel 中写入数据才会触发出站处理器；出站是按照加入 Pipeline 的顺序反向执行的
 * <p>
 * 日志：
 * 12:01:32 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.c.TestPipeline - 1
 * 12:01:32 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.c.TestPipeline - 2
 * 12:01:32 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.c.TestPipeline - 3
 * 12:01:32 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.c.TestPipeline - 6
 * 12:01:32 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.c.TestPipeline - 5
 * 12:01:32 [DEBUG] [nioEventLoopGroup-2-2] c.i.n.c.TestPipeline - 4
 */
@Slf4j
public class TestPipeline {

    public static void main(String[] args) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {

                        // 1. 通过 channel 拿到 pipeline
                        ChannelPipeline pipeline = ch.pipeline();

                        // 2. 添加处理器 head ->  h1 -> h2 ->  h4 -> h3 -> h5 -> h6 -> tail
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("1");
                                super.channelRead(ctx, msg); // 将 msg 传给下一个 handler
                            }
                        });

                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
                                log.debug("2");
                                Student student = new Student(name.toString());
// 将数据传递给下个 handler，如果不调用，调用链会断开 或者调用 ctx.fireChannelRead(student);
//                                super.channelRead(ctx, name);
                                super.channelRead(ctx, student);
                            }
                        });

                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                log.debug("3");
                                log.debug("3, 结果: {}, class: {}", msg, msg.getClass());
                                // head ->  h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                                // ctx#writeAndFlush 查找顺序：h3->h2->h1
                                // ch#writeAndFlush 查找顺序：h6->h5->h4
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
//                                ch.writeAndFlush(ctx.alloc().buffer().writeBytes("server...".getBytes()));
                            }
                        });


                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });

                        pipeline.addLast("h6", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(8080);
    }

    @Data
    @AllArgsConstructor
    static class Student {

        private String name;

    }

}
