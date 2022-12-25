package cn.itcast.rpc.client;

import cn.itcast.rpc.client.handler.RpcResponseMessageHandler;
import cn.itcast.rpc.message.RpcRequestMessage;
import cn.itcast.rpc.protocol.MessageCodecSharable;
import cn.itcast.rpc.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>RPC客户端</h2>
 */
@Slf4j
public class RpcClient {

    // 既是入站处理器也是出站处理器
    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
    // 既是入站处理器也是出站处理器
    private static final MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
    // 入站处理器
    private static final RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });

            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();

            // Channel建立以后发送请求消息
            // Channel#writeAndFlush也是异步的
            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(
                    1,
                    "cn.itcast.rpc.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"张三"}
            )).addListener(promise -> {
                // 这里的promise和上面的future是同一个对象
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();
                    log.error("error", cause);
                }
            });

            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }

}
