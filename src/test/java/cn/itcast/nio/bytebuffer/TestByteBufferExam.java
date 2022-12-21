package cn.itcast.nio.bytebuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * ByteBuffer综合练习
 * <P></P>
 * 网络上有多条数据发送给服务端，数据之间使用"\n"进行分隔
 * <p></p>
 * 但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为:
 * <p></p>
 * Hello,world\n
 * <p></p>
 * I'm zhangsan\n
 * <p></p>
 * How are you?\n
 * <p></p>
 * 变成了下面的两个byteBuffer:
 * <p></p>
 * Hello,world\nI'm zhangsan\nHo
 * <p></p>
 * w are you?\n
 * <p></p>
 * 现在要求你编写程序，将错乱的数据恢复成原始的按"\n"分隔的数据
 * <p></p>
 */
public class TestByteBufferExam {

    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        // 第一次接收消息
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes(StandardCharsets.UTF_8));
        split(source);
        // 第二次接收消息
        source.put("w are you?\n".getBytes(StandardCharsets.UTF_8));
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip(); // 切换成读模式
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
//                System.out.println("length = " + length);
                // 把这条完整消息存入新的ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从source读，向target写length个字节
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact(); // 本次未读到的消息与下次消息拼接成一条完整的消息
    }

}
