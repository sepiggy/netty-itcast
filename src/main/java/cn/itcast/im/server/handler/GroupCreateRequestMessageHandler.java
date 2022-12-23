package cn.itcast.im.server.handler;

import cn.itcast.im.message.GroupCreateRequestMessage;
import cn.itcast.im.message.GroupCreateResponseMessage;
import cn.itcast.im.server.session.Group;
import cn.itcast.im.server.session.GroupSession;
import cn.itcast.im.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {

        // 群名
        String groupName = msg.getGroupName();
        // 群成员
        Set<String> members = msg.getMembers();
        // 群管理器
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        // 创建群
        Group group = groupSession.createGroup(groupName, members);
        if (group == null) {
            // 向群创建者发送创建群聊成功消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, groupName + "创建成功"));
            // 向群成员发送拉群消息
            List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入" + groupName));
            }
        } else {
            // 向群创建者发送创建群聊失败消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, groupName + "已经存在"));
        }
    }

}
