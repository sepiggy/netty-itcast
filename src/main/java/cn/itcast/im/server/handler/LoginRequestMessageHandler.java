package cn.itcast.im.server.handler;

import cn.itcast.im.message.LoginRequestMessage;
import cn.itcast.im.message.LoginResponseMessage;
import cn.itcast.im.server.service.UserServiceFactory;
import cn.itcast.im.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <h2>登录请求业务处理Handler</h2>
 * <pre>
 * 登录请求业务处理Handler只关心登录请求，即LoginRequestMessage，这里可以使用 {@link SimpleChannelInboundHandler} 只处理特定类型的消息
 * </pre>
 */
@ChannelHandler.Sharable
@Slf4j
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        // 登录业务处理
        String username = msg.getUsername();
        String password = msg.getPassword();
        boolean login = UserServiceFactory.getUserService().login(username, password);
        // 登录响应消息
        LoginResponseMessage message;
        if (login) {
            // 保存会话信息，绑定Session和用户名
            SessionFactory.getSession().bind(ctx.channel(), username);
            message = new LoginResponseMessage(true, "登录成功");
            List<String> onlineUsernames = SessionFactory.getSession().getOnlineUsernames();
            log.debug("当前所有在线用户: {}", onlineUsernames.stream().collect(Collectors.joining(",", "[", "]")));
        } else {
            message = new LoginResponseMessage(false, "用户名或密码不正确");
        }
        // 出站
        ctx.writeAndFlush(message);
    }

}
