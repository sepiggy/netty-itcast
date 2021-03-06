package cn.itcast.nio.c2;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;

/**
 * ByteBuffer 与 字符串之间的相互转换
 * 推荐使用
 * StandardCharsets.UTF_8#encode
 * StandardCharsets.UTF_8#decode
 * 这一组方法完成字符串和 ByteBuffer 之间的转换，且转换完以后自动切换为读模式
 */
public class TestByteBufferString {
    public static void main(String[] args) {
        // 一） 字符串 -> ByteBuffer
        // 1. put, 不自动切换为读模式
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("hello".getBytes()); // 当前还是写模式
        debugAll(buffer1);

        // 2. Charset, 自动切换为读模式
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello"); // 已经切换为读模式
        debugAll(buffer2);

        // 3. wrap, 自动切换为读模式, 自动切换为读模式
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes()); // 已经切换为读模式
        debugAll(buffer3);

        // 二） ByteBuffer -> 字符串
        // buffer2现在是读模式可以直接转为字符串
        String str1 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(str1);

        // buffer1现在是写模式不能直接转换为字符串
        buffer1.flip();
        String str2 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(str2);
    }
}
