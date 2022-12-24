package cn.itcast.im.server.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.itcast.im.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>p116-退出处理器</h2>
 * <pre>
 * 退出分为正常退出和异常退出
 * </pre>
 */
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    // 正常退出
    // 当连接断开时触发inactive事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除会话
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 已经断开", ctx.channel());
    }

    // 异常退出
    // 当出现异常时触发exceptionCaught事件
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 已经异常断开, 异常是 {}", ctx.channel(), ExceptionUtil.stacktraceToString(cause, Integer.MAX_VALUE));
    }

}
