package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import static cn.itcast.nio.c2.ByteBufferUtil.debugAll;
import static cn.itcast.nio.c2.ByteBufferUtil.debugRead;

/**
 * 处理消息边界
 */
@Slf4j
public class Server_04 {

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

    public static void main(String[] args) throws IOException {

        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        // 2. 建立 selector 和 channel 的联系 (注册)
        // 通过 SelectionKey 可以知道将来哪个 Channel 发生哪个事件
        // 四种事件类型：
        // accept - 会在有连接请求时触发
        // connect - 客户端连接建立后触发
        // read - 可读事件
        // write - 可写事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // sscKey 只关注 accpet 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey: {}", sscKey);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. select 方法
            // 没有事件发生，线程阻塞；有事件，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞
            // 事件发生后要么处理，要么取消，不能置之不理; 否则陷入死循环
            selector.select();
            // 4. 处理事件, selectedKeys 的返回值包含了所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 处理完一个key一定要从迭代器中删除
                iterator.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                // 无论是正常断开还是异常断开，都会触发一次读事件
                // 所以针对这两种情况都要分别做处理，否则服务端会因为
                // 你没有处理这次读事件不停地陷入死循环
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                    log.debug("scKey: {}", scKey);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel(); // 拿到触发事件的 channel
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = channel.read(buffer);
                        if (read == -1) {  // 如果是正常断开，read的方法返回值是-1
                            key.cancel();
                        } else {
                            split(buffer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel(); // 如果是异常断开，因为客户端断开了，因此需要反注册
                    }
                }
//                key.cancel();
            }
        }
    }
}
