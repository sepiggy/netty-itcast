package cn.itcast.nio.bytebuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * 掌握分散读取的思想: 减少ByteBuffer之间数据的拷贝，提高效率
 * <p></p>
 * FileChannel#read(ByteBuffer[] dsts) throws IOException
 * <p></p>
 * 读取FileChannel的数据依次写入dsts数组
 */
public class TestScatteringReads {

    public static void main(String[] args) {
        // words.text 内容: onetwothree
        // 通过"RandomAccessFile#getChannel"来获取Channel, 读取操作需要r模式打开文件
        try (FileChannel fileChannel = new RandomAccessFile("words.txt", "r").getChannel()) {

            // 构造三个ByteBuffer实例
            ByteBuffer b1 = ByteBuffer.allocate(3);
            ByteBuffer b2 = ByteBuffer.allocate(3);
            ByteBuffer b3 = ByteBuffer.allocate(5);

            // 将FileChannel中的数据依次读取到三个ByteBuffer里
            fileChannel.read(new ByteBuffer[]{b1, b2, b3});

            // 切换为读模式
            b1.flip();
            b2.flip();
            b3.flip();

            debugAll(b1);
            debugAll(b2);
            debugAll(b3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
