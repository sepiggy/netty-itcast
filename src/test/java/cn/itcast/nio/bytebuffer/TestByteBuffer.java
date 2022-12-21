package cn.itcast.nio.bytebuffer;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 使用ByteBuffer和FileChannel相关API读取"data.txt"文件
 */

/**
 * ByteBuffer的正确使用方式:
 * 1. 向 buffer 写入数据，例如调用 channel.read(buffer)
 * 2. 调用 flip() 切换至读模式
 * 3. 从 buffer 读取数据，例如调用 buffer.get()
 * 4. 调用 clear() 或 compact() 切换至写模式
 * 5. 重复 1~4 步骤
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        // FileChannel
        // 通过以下两种方式可以获取 FileChannel:
        // 1. 输入流(FileInputStream)或输出流(FileOutputStream)
        // 2. RandomAccessFile
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 准备缓冲区, 通过 ByteBuffer 静态方法 (ByteBuffer 不能通过 new 获取一个引用)
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) { // 多次不断地从 channel 读取数据，向 buffer 写入，直到 buffer 被填满然后调用 buffer#clear 方法继续向 buffer 中写数据
                int len = channel.read(buffer);
                log.debug("读取到的字节数 {}", len);
                if (len == -1) { // 没有内容了
                    break;
                }
                /**
                 * 打印 buffer 的内容
                 */
                buffer.flip(); // 切换至读模式
                while (buffer.hasRemaining()) { // 是否还有剩余未读数据
                    byte b = buffer.get(); // buffet#get 读取一个字节
                    log.debug("实际字节 {}", (char) b);
                }
                buffer.clear(); // 切换至写模式
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
