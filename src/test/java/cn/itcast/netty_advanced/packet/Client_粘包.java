package cn.itcast.netty_advanced.packet;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 演示粘包现象
 */
public class Client_粘包 {

    static final Logger log = LoggerFactory.getLogger(Client_粘包.class);

    public static void main(String[] args) {

        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        // 在连接Channel建立成功后，会触发active事件
                        // 与之前在客户端使用sync方式效果是一样的
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {

                            // ATTN 什么是粘包现象？
                            // 客户端向服务端发送了10次数据 （调用了10次writeAndFlush)
                            // 每次发送16B
                            // 而客户端会一次性接收160B数据，这就是粘包现象
                            // EventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0x289fde34, L:/127.0.0.1:8080 - R:/127.0.0.1:42964] READ: 160B
                            for (int i = 0; i < 10; i++) {
                                // 在 Handler 中最好使用 ctx 来分配 ByteBuf
                                ByteBuf buf = ctx.alloc().buffer(16);
                                buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                ctx.writeAndFlush(buf);
                            }
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

}