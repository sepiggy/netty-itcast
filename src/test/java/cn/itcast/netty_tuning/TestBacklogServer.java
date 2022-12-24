package cn.itcast.netty_tuning;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * <h2>演示ServerSocketChannel的ChannelOption.SO_BACKLOG参数</h2>
 * <pre>
 * ChannelOption.SO_BACKLOG参数配置服务端的全连接队列大小
 * ChannelOption.SO_BACKLOG参数生产环境尽可能设置大一些
 * </pre>
 */
public class TestBacklogServer {

    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .option(ChannelOption.SO_BACKLOG, 2) // 测试全队列满了
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new LoggingHandler());
                    }
                }).bind(8080);
    }

}
