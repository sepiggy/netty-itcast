package cn.itcast.protocol;

import cn.itcast.message.LoginRequestMessage;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * 测试自定义协议
 */
public class TestMessageCodec {

    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 避免半包
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(),
                new MessageCodec());

        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        channel.writeOutbound(message);
    }

}
