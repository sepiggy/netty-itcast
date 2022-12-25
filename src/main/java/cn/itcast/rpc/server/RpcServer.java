package cn.itcast.rpc.server;

import cn.itcast.rpc.protocol.MessageCodecSharable;
import cn.itcast.rpc.protocol.ProcotolFrameDecoder;
import cn.itcast.rpc.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC服务端
 */
@Slf4j
public class RpcServer {

    // 既是入站处理器也是出站处理器
    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
    // 既是入站处理器也是出站处理器
    private static final MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
    // 入站处理器
    private static final RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();

    public static void main(String[] args) {

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 处理半包、粘包的解码器
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    // 处理日志处理器
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 自定义协议编解码器
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // RPC请求消息处理器
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
