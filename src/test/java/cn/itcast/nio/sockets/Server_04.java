package cn.itcast.nio.sockets;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * 使用“NIO+非阻塞+多路复用(Selector)+单线程"解决Server端"处理消息边界“问题
 * <br>
 * 使用run模式运行服务端
 * <br>
 * 在{@link Server_03}基础上处理消息边界
 * <br>
 * 这里使用分隔符方式(\n)处理消息边界，遇到"\n"就拆分为一条完整的消息
 * <br>
 * 结论：
 * <br>
 * 这个Demo里发送的"完整消息长度"(以\n为边界)若超过ByteBuffer的容量，会出现两个问题：
 * <br>
 * 1) 不会报错，但会丢失消息
 * <br>
 * 2) 触发多次读事件
 * <br>
 * 解决方案 {@link Server_05}：
 * <br>
 * 1) ByteBuffer需要支持动态扩容
 * <br>
 * 2) 增加ByteBuffer的生命周期, 以保证发生多次读事件时用同一个ByteBuffer接收
 */
@Slf4j
public class Server_04 {

    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey: {}", sscKey);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                log.debug("key: {}", key);
                if (key.isAcceptable()) {
                    log.debug("有accept事件发生");
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("SocketChannel: {}", sc);
                    log.debug("SelectionKey: {}", scKey);
                } else if (key.isReadable()) {
                    log.debug("有read事件发生");
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 演示消息边界问题 (ByteBuffer容量不足)
//                        ByteBuffer buffer = ByteBuffer.allocate(4);
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = channel.read(buffer);
                        if (read == -1) {
                            key.cancel();
                        } else {
//                            buffer.flip();
//                            System.out.println("Charset.defaultCharset().decode(buffer).toString() = " + Charset.defaultCharset().decode(buffer).toString());
                            split(buffer); // 处理消息边界
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();
                    }
                }
            }
        }
    }

    private static void split(ByteBuffer source) {
        source.flip(); // 切换读模式
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact(); // 切换写模式
    }

}
