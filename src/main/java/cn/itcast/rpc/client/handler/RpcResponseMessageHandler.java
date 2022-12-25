package cn.itcast.rpc.client.handler;

import cn.itcast.rpc.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h2>客户端RPC响应消息入站处理器</h2>
 * <pre>
 * Promise是一个容器，通过Promise实现主线程和NioEventLoop线程通信
 * </pre>
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    // key: 序号，value：用来接收结果的Promise对象
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.debug("{}", msg);
        // 拿到空的Promise，塞入结果后这个Promise就没有用了，因此这里使用remove方法
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (promise != null) {
            // 塞入结果
            Object returnValue = msg.getReturnValue();
            Exception exceptionValue = msg.getExceptionValue();
            if (exceptionValue != null) {
                promise.setFailure(exceptionValue);
            } else {
                promise.setSuccess(returnValue);
            }
        }
    }

}
