package cn.itcast.nio.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static cn.itcast.nio.c2.ByteBufferUtil.debugRead;

/**
 * Selector版本Server
 * 解决 CPU 空转问题
 */
@Slf4j
public class Server_03 {
    public static void main(String[] args) throws IOException {

        // 1. 创建 selector, 管理多个 channel, 需要将 channel 注册到 selector 上
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
        // sscKey 只关注 accept 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey: {}", sscKey);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. select 方法
            // 没有事件发生，线程阻塞；有事件发生，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞
            // 事件发生后要么处理 (调用 accept 或 read 方法等)，要么取消 (调用 cancel 方法)，不能置之不理; 否则陷入死循环
            selector.select();
            // 4. 处理事件, selectedKeys 的返回值包含了所有发生的事件
            // 这里后面需要对 selectionKey 进行删除操作，因此这里只能用迭代器进行遍历，不能使用增强 for 进行遍历
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
                if (key.isAcceptable()) { // 处理 ServerSocketChannel 连接请求事件
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                    log.debug("scKey: {}", scKey);
                } else if (key.isReadable()) { // 处理 SocketChannel 读事件
                    try {
                        SocketChannel channel = (SocketChannel) key.channel(); // 拿到触发事件的 channel
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = channel.read(buffer);
                        if (read == -1) {  // 如果是正常断开，read的方法返回值是-1
                            key.cancel();
                        } else {
                            buffer.flip();
                            debugRead(buffer);
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
