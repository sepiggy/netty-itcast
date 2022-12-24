package cn.itcast.netty_basic.hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * HelloWorld服务端
 * <br>
 * 开发一个简单的服务器端和客戶端:
 * <br>
 * 1) 客戶端向服务器端发送 hello, world
 * <br>
 * 2) 服务器仅接收，不返回
 */
public class HelloServer {

    public static void main(String[] args) {
        // 1. ServerBootstrap: 服务端启动器，负责组装netty组件，启动服务器
        new ServerBootstrap()
                // 2. NIO事件循环组
                /**
                 * EventLoopGroup对应NIO网络编程中的选择器(Selector)+线程(Thread) {@link cn.itcast.nio.sockets.Server_06}
                 * 一个NioEventLoopGroup由一个BossEventLoop和多个WorkerEventLoop组成
                 * 一个EventLoop由一个Selector和一个Thread组成
                 * 一个EventLoopGroup由多个EventLoop组成
                 * 注意：BossEventLoop监听accept事件，而accept事件处理器由netty内部实现，其处理逻辑是调用"ChannelInitializer#initChannel"方法
                 * 注意：WorkerEventLoop监听read和write事件，其事件处理器由程序员在"ChannelInitializer#initChannel"里进行添加
                 */
                .group(new NioEventLoopGroup()) // 关注accept事件，accept事件由netty内部进行处理
                // 3. 选择服务端的ServerSocketChannel实现(对原生的ServerSocketChannel进行了封装)
                //    对应NIO编程中的如下代码：
                //        ServerSocketChannel ssc = ServerSocketChannel.open();
                //        ssc.configureBlocking(false);
                .channel(NioServerSocketChannel.class)
                // 4. 添加读写事件发生时的事件处理器（Handler）
                //    Boss: 负责处理连接
                //    Worker(Child): 负责处理读写
                //    ChildHandler: 决定了Worker(Child)在read和write事件发生时能执行哪些操作
                .childHandler(
                        // 5. 添加"Channel初始化器"
                        //    注意：ChannelInitializer(Channel初始化器)是一种对Channel进行初始化的特殊的事件处理器(Handler)
                        //    注意：ChannelInitializer会在连接建立后(即accept事件发生后)执行initChannel方法
                        //    名词解释:
                        //    Channel: 代表和客户端进行数据读写的通道
                        //    Initializer: 初始化器，负责初始化别的Handler(是一种特殊的Handler)
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                // 6. 添加具体的Handler
                                // 注意：在Netty中收发数据都会走Handler

//                                ch.pipeline().addLast(new LoggingHandler()); // 日志处理器
                                ch.pipeline().addLast(new StringDecoder()); // 解码器，数据存储在ByteBuf中，将ByteBuf转换为字符串
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() { // 自定义Handler
                                    /**
                                     * 当Channel上有读事件发生时，执行如下操作
                                     * @param ctx
                                     * @param msg 拿到的是上一步经过StringDecoder转换好的字符串
                                     * @throws Exception
                                     */
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        System.out.println(msg); // 打印上一步转换好的字符串
                                    }
                                });
                            }
                        })
                // 7. 绑定监听端口
                .bind(8080);
    }

}
