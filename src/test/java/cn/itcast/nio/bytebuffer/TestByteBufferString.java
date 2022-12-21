package cn.itcast.nio.bytebuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * ByteBuffer与字符串之间的相互转换
 * <p></p>
 * 推荐使用:
 * <p></p>
 * StandardCharsets.UTF_8#encode
 * <p></p>
 * StandardCharsets.UTF_8#decode
 * <p></p>
 * 这一组方法完成字符串和 ByteBuffer 之间的转换，且转换完以后自动切换为读模式
 */
public class TestByteBufferString {

    public static void main(String[] args) {
        // 一） 字符串 -> ByteBuffer
        // 1. put, 不自动切换为读模式
        ByteBuffer buffer1 = ByteBuffer.allocate(16); // position:0, limit:16, capacity:16
        buffer1.put("hello".getBytes(StandardCharsets.UTF_8)); // 当前还是写模式, position:5, limit:16, capacity:16
        debugAll(buffer1);

        // 2. Charset, 自动切换为读模式
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello"); // 已经切换为读模式, position:0, limit:5
        debugAll(buffer2);

        // 3. wrap, 自动切换为读模式
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8)); // 已经切换为读模式, position:0, limit:5
        debugAll(buffer3);

        // 二） ByteBuffer -> 字符串
        // buffer2现在是读模式可以直接转为字符串
        String str1 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(str1);

        // buffer1现在是写模式不能直接转换为字符串
        buffer1.flip(); // 切换为读模式
        String str2 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(str2);
    }

}
