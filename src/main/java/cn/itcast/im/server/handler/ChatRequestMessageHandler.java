package cn.itcast.im.server.handler;

import cn.itcast.im.message.ChatRequestMessage;
import cn.itcast.im.message.ChatResponseMessage;
import cn.itcast.im.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * <h2>发送单聊消息处理器</h2>
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 消息发给谁的用户名
        String to = msg.getTo();
        // 获取用户名对应的Channel
        Channel channel = SessionFactory.getSession().getChannel(to);
        // 在线
        if (channel != null) {
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        // 不在线
        else {
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方用户不存在或者不在线"));
        }
    }

}
