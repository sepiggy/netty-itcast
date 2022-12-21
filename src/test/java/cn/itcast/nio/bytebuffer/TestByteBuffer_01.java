package cn.itcast.nio.bytebuffer;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 优化版
 */
@Slf4j
public class TestByteBuffer_01 {

    public static void main(String[] args) {
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(10);
            int len;
            while ((len = channel.read(buffer)) != -1) {
                log.debug("读取到的字节数 {}", len);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    log.debug("实际字节 {}", (char) buffer.get());
                }
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
