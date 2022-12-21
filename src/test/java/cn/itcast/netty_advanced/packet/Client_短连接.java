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
 * <h2>粘包半包解决方案1-短链接(客户端)</h2>
 * <pre>
 * 以连接建立和连接断开作为消息边界
 * 短连接发送完数据就断开，是不会造成粘包现象的
 * 缺点：
 * 1) 效率低
 * 2) 短连接并不能解决半包问题
 * </pre>
 */
public class Client_短连接 {

    static final Logger log = LoggerFactory.getLogger(Client_短连接.class);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            send(); // 通过多次调用send方法来实现发送多条数据
        }
        System.out.println("finish");
    }

    /**
     * 短连接发送：客户端发送完数据就断开
     */
    private static void send() {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            ByteBuf buf = ctx.alloc().buffer(16);
                            // 用来演示粘包
                            // buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

                            // 用来演示半包
                            buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17});
                            ctx.writeAndFlush(buf);
                            ctx.channel().close(); // 客户端发送完消息就断开
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