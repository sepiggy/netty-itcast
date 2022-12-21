package cn.itcast.netty_advanced.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * <h2>使用EmbeddedChannel来测试LTC解码器</h2>
 * {@link LengthFieldBasedFrameDecoder}
 */
public class TestLengthFieldDecoder {

    static final int MAGIC_NUM = 0xCAFE;

    public static void main(String[] args) {

        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(
                        1024, 0, 4, 1, 5),
                new LoggingHandler(LogLevel.DEBUG)
        );

        // 内容长度(4个字节) + 内容
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer, "Hello, world");
        send(buffer, "Hi!");
        send(buffer, "running edge of the world");
        channel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes(); // 实际内容
        int length = bytes.length; // 实际内容长度
        // 先写4个字节，表示内容长度
        buffer.writeInt(length);
        // 再写魔数
        buffer.writeByte(MAGIC_NUM);
        // 最后写实际内容
        buffer.writeBytes(bytes);
    }

}

