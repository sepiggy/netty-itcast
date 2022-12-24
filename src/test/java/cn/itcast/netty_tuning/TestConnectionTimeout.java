package cn.itcast.netty_tuning;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>客户端连接建立超时参数</h2>
 * <pre>
 * 1) Netty配置参数方式：
 * 1.1) 客户端配置参数
 *    通过Bootstrap#option方法给SocketChannel配置参数
 * 1.2) 服务端配置参数
 *    通过ServerBootstrap#option方法给ServerSocketChannel配置参数
 *    通过ServerBootstrap#childOption方法给SocketChannel配置参数
 * 2) ChannelOption.CONNECT_TIMEOUT_MILLIS选项用于给客户端配置参数，若指定时间内客户端没有和服务端建立连接会出现超时异常
 * </pre>
 */
@Slf4j
public class TestConnectionTimeout {

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler());
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8080);
            future.sync().channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("timeout");
        } finally {
            group.shutdownGracefully();
        }
    }

}
