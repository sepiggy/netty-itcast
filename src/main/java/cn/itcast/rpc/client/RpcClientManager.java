package cn.itcast.rpc.client;

import cn.itcast.rpc.client.handler.RpcResponseMessageHandler;
import cn.itcast.rpc.message.RpcRequestMessage;
import cn.itcast.rpc.protocol.MessageCodecSharable;
import cn.itcast.rpc.protocol.ProcotolFrameDecoder;
import cn.itcast.rpc.protocol.SequenceIdGenerator;
import cn.itcast.rpc.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * <h2>RPC客户端(升级版)</h2>
 * <pre>
 * 1) 复用Channel，每个RPC请求都共用一个Channel，将获取Channel和发送RPC请求解耦
 * 2) 优化调用方式，添加代理类，自动封装消息对象
 * </pre>
 */
@Slf4j
public class RpcClientManager {

    // 打印日志处理器 (入站 + 出站)
    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
    // 自定义消息解码器 (入站 + 出站)
    private static final MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
    // RPC消息响应处理器 (入站)
    private static final RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
    private static Channel channel = null;
    private static final Object LOCK = new Object();

    public static void main(String[] args) {
        // 获取代理对象
        HelloService service = getProxyService(HelloService.class);
        // 通过RPC调用方法
        String result = service.sayHello("zhangsan");
        System.out.println("result = " + result);
    }

    /**
     * 创建serviceClass接口代理实现对象
     * 将方法调用转换成RPC请求消息
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxyService(Class<T> serviceClass) {
        // 获取接口类型的类加载器
        ClassLoader loader = serviceClass.getClassLoader();
        // 代理类要实现的接口
        Class<?>[] interfaces = new Class[]{serviceClass};
        // JDK代理
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为RpcRequestMessage消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            // 2. 将消息对象发送出去
            getChannel().writeAndFlush(msg);

            // 3. 主线程准备一个空Promise对象来等待NioEventLoop线程塞入结果(参数：指定Promise对象异步接收结果的线程)
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            // 这种获取结果方式也可
            /*
            promise.addListener(future -> {
                // 这里接收结果的线程是构造DefaultPromise对象时传入的线程
            });
            */

            // 4. 等待Promise结果
            promise.await();
            if (promise.isSuccess()) {
                // 调用正常
                return promise.getNow();
            } else {
                // 调用失败
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }

    // 获取唯一的Channel对象
    // Channel对象只有一个，只会被初始化一次
    // 不同的RPC请求共享一个Channel即可
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        // double-check
        synchronized (LOCK) { // t2
            if (channel != null) { // t1
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 初始化Channel方法
    private static void initChannel() {

        NioEventLoopGroup group = new NioEventLoopGroup();

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
        try {
            // 同步等待Channel建立完毕
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            // 异步添加Channel关闭逻辑
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }

}
