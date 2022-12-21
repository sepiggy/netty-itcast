package cn.itcast.nio.bytebuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * 掌握集中写入的思想: 减少ByteBuffer之间数据的拷贝，提高效率
 * <p></p>
 * FileChannel#write(ByteBuffer[] srcs) throws IOException
 * <p></p>
 * 依次将srcs数组中的ByteBuffer写入FileChannel
 */
public class TestGatheringWrites {

    public static void main(String[] args) {
        ByteBuffer b1 = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer b2 = StandardCharsets.UTF_8.encode("world");
        ByteBuffer b3 = StandardCharsets.UTF_8.encode("你好");

        // 写入操作需要"rw"模式打开文件
        try (FileChannel channel = new RandomAccessFile("words2.txt", "rw").getChannel()) {
            channel.write(new ByteBuffer[]{b1, b2, b3});
        } catch (IOException e) {
        }

        debugAll(b1);
        debugAll(b2);
        debugAll(b3);
    }

}
