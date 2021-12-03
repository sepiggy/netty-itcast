package cn.itcast.netty.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * Netty服务端类
 */
public class HelloServer {
    public static void main(String[] args) {
        // 1. ServerBootstrap: 服务端启动器，负责组装 netty 组件，启动服务器
        new ServerBootstrap()
            // 2. NioEventLoopGroup = BossEventLoop + Worker(Child)EventLoop
            // 其中 WorkerEventLoop = (Selector * 1 + Thread * 1) * 多个
            .group(new NioEventLoopGroup())
            // 3. 选择服务端的 ServerSocketChannel 实现 (对原生的 ServerSocketChannel 进行封装)
            .channel(NioServerSocketChannel.class) // OIO BIO
            // 4. Boss: 负责处理连接
            //    Worker(Child): 负责处理读写
            //    ChildHandler: 决定了 Worker(Child) 能执行哪些操作
            .childHandler(
                    // 5. Channel: 代表和客户端进行数据读写的通道
                    //    Initializer: 初始化器，负责添加别的 Handler (是一种特殊的 Handler)
                new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    // 6. 添加具体 Handler
                    ch.pipeline().addLast(new LoggingHandler());
                    ch.pipeline().addLast(new StringDecoder()); // 将 ByteBuf 转换为字符串
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() { // 自定义 Handler
                        @Override // 读事件
                        public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception {
                            System.out.println(msg); // 打印上一步转换好的字符串
                        }
                    });
                }
            })
            // 7. 绑定监听端口
            .bind(8080);
    }
}
