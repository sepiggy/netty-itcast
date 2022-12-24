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
 * 使用“NIO+非阻塞+多路复用(Selector)+单线程"解决Server端"消息完整性"问题
 * <br>
 * 使用run模式运行服务端
 * <br>
 * 在 {@link Server_04} 的基础上解决"消息完整性"问题
 * <br>
 * 附件与扩容: 解决消息长度超过ByteBuffer容量的问题来保证每次都能读取一条完整的消息
 * <br>
 * 要点：
 * <br>
 * 1) ByteBuffer需要支持动态扩容
 * <br>
 * 2) 增加ByteBuffer的生命周期, 以保证发生多次读事件时用同一个ByteBuffer接收(使用附件)
 * <br>
 * 3) 一个SocketChannel对应一个ByteBuffer
 */
@Slf4j
public class Server_05 {

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
                    log.debug("发生了accept事件");
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(16); // attachment
                    // 注意：将一个ByteBuffer作为附件关联到SelectionKey上 (SocketChannel#register的第三个参数）
                    // 注意：以保证每个SocketChannel对应唯一的ByteBuffer
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("SocketChannel: {}", sc);
                    log.debug("SelectionKey: {}", scKey);
                } else if (key.isReadable()) {
                    log.debug("发生了read事件");
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 获取SelectionKey上关联的附件(ByteBuffer)
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer);
                        if (read == -1) {
                            key.cancel();
                        } else {
                            split(buffer);
                            if (buffer.position() == buffer.limit()) { // 说明在一次split操作中没有找到一条完整的消息，需要扩容
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2); // 扩容翻倍
                                buffer.flip(); // 切换为读模式
                                newBuffer.put(buffer); // 将旧的ByteBuffer的内容拷贝到新的ByteBuffer中
                                key.attach(newBuffer); // attach方法可以关联新的附件，将扩容后的ByteBuffer关联到SelectionKey
                            }
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
        source.flip();
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
        source.compact();
    }

}
