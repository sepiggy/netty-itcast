package cn.itcast.rpc.protocol;

import cn.itcast.rpc.config.Config;
import cn.itcast.rpc.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <h2>@Sharable注解说明</h2>
 * <pre>
 * 1) MessageToMessageCodec的子类如果自己判断是无状态的可以标记为@Sharable
 * 2) 必须和 {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder} 一起使用，以确保接到的ByteBuf消息是完整的
 * </pre>
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1) 4字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 2) 1字节的版本,
        out.writeByte(1);
        // 3) 1字节的序列化方式: jdk-0 , json-1
        // 从配置文件读取选择的序列化算法
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4) 1字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5) 4个字节
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        out.writeByte(0xff);
        // 6) 获取内容的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 7) 长度
        out.writeInt(bytes.length);
        // 8) 写入内容
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerAlgorithm = in.readByte(); // 0 或 1
        byte messageType = in.readByte(); // 0,1,2...
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // 找到反序列化算法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerAlgorithm];
        // 确定具体消息类型
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        Message message = algorithm.deserialize(messageClass, bytes);
        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerAlgorithm, messageType, sequenceId, length);
        log.debug("{}", message);
        out.add(message);
    }

}
