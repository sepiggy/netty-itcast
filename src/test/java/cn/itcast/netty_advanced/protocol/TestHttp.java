package cn.itcast.netty_advanced.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * <h2>使用Netty实现Http服务端</h2>
 * <pre>
 * 1. Netty内置了Http编解码器，程序员只需要关心业务处理即可
 * 2. 以"Codec"结尾的类通常既包括解码(Decoder)又包括编码(Encoder)
 * 3. SimpleChannelInboundHandler是这样一种入站处理器：其只关心某种类型的消息，可以根据消息的类型加以区分以选择处理
 * </pre>
 */
@Slf4j
public class TestHttp {

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
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    // Netty专门提供的HTTP编解码器
                    ch.pipeline().addLast(new HttpServerCodec());
                    // 对Http请求进行处理
                    // SimpleChannelInboundHandler是这样一种入站处理器：
                    // 其只关心某种类型的消息，这里只关心HttpRequest类型的消息
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            // 这里拿到的msg就只是HttpRequest类型
                            // 获取请求行
                            log.debug(msg.uri());

                            // 返回响应
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                            byte[] bytes = "<h1>Hello, world!</h1>".getBytes(StandardCharsets.UTF_8);
                            response.headers().setInt(CONTENT_LENGTH, bytes.length);
                            response.content().writeBytes(bytes);
                            // 写回响应
                            ctx.writeAndFlush(response);
                        }
                    });
                    /*
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            // Netty的HttpServerCodec请求解码器会把Http请求解析为两部分:
                            // DefaultHttpRequest -> 请求行，请求头
                            // LastHttpContent -> 请求体
                            // 需要对这两部分分别处理
                            // 可以使用 instanceof 判断类型分别作处理，如下所示，更好的方式是使用 SimpleChannelInboundHandler (见上)
                            log.debug("{}", msg.getClass());
                            if (msg instanceof HttpRequest) { // 请求行，请求头

                            } else if (msg instanceof HttpContent) { // 请求体

                            }
                        }
                    });
                    */
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
