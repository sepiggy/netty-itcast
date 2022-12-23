package cn.itcast.im.protocol;

import cn.itcast.im.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * <h2>自定义协议</h2>
 * <pre>
 * 1) {@link ByteToMessageCodec} 表示将ByteBuf与自定义消息类型进行转化
 * 2) MessageCodec编解码器将ByteBuf与消息进行转化，编解码器的编写过程就是自定义协议的过程
 * 3) 自定义协议通常包括以下几个要素：
 * 魔数，用来在第一时间判定是否是无效数据包
 * 版本号，可以支持协议的升级
 * 序列化算法，消息正文到底采用哪种序列化反序列化方式，可以由此扩展，例如：json、protobuf、hessian、jdk
 * 指令类型，是登录、注册、单聊、群聊... 跟业务相关
 * 请求序号，为了双工通信，提供异步能力
 * 正文长度
 * 消息正文
 * 4) 有状态的Handler不能被多个Channel共享，线程不安全，必须创建多个实例，eg. {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder}.
 *    没有状态的Handler可以被多个Channel共享，线程安全，Netty内置的可共享的Handler会被注解 {@link io.netty.channel.ChannelHandler.Sharable} 标记，可以只创建一个实例，eg. {@link io.netty.handler.logging.LoggingHandler}.
 * 5) Be aware that sub-classes of ByteToMessageCodec MUST NOT annotated with @Sharable.
 * </pre>
 */
@Slf4j
//@ChannelHandler.Sharable // 继承自ByteToMessageCodec的类添加@Sharable注解会抛异常
public class MessageCodec extends ByteToMessageCodec<Message> {

    /**
     * 编码 Message -> ByteBuf
     *
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 1) 4字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 2) 1字节的版本
        out.writeByte(1);
        // 3) 1字节的序列化方式: jdk-0, json-1
        out.writeByte(0);
        // 4) 1字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5) 4个字节的请求序号
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充 (保证除去内容外前面固定部分长度为2的幂次方)
        out.writeByte(0xff);
        // 6) 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 7) 4个字节长度
        out.writeInt(bytes.length);
        // 8) 写入内容
        out.writeBytes(bytes);
    }

    /**
     * 解码 ByteBuf -> Message
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte(); // 跳过padding
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length); // 读入实际内容数组
        if (serializerType == 0) { // 0表示需要使用JDK反序列化方式
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Message message = (Message) ois.readObject(); // 反序列化
            log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
            log.debug("{}", message);
            out.add(message); // 解析一条消息之后要装入out这个list以便给下一个Handler使用
        }
    }

}