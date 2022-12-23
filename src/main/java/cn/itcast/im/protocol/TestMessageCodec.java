package cn.itcast.im.protocol;

import cn.itcast.im.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * <h2>使用EmbeddedChannel测试自定义协议的编解码器</h2>
 */
public class TestMessageCodec {

    public static void main(String[] args) throws Exception {

        EmbeddedChannel channel = new EmbeddedChannel(
                new LoggingHandler(),
                // 避免粘包、半包
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new MessageCodec());

        // encode
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");

        // 出站
//        channel.writeOutbound(message);

        // decode
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);

        // 入站
        // channel.writeInbound(buf);

        // 使用切片模拟半包现象
        ByteBuf s1 = buf.slice(0, 100);
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);

        s1.retain();

        // 只写s1
        channel.writeInbound(s1);
        channel.writeInbound(s2);
    }

}
