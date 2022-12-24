package cn.itcast.nio.sockets;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * 演示可写事件服务端 (大量数据一次写不完的情况)
 * <br>
 * 服务端向客户端写数据
 */
@Slf4j
public class WriteServer {

    public static void main(String[] args) throws IOException {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        // 注意： 在注册时就可以关注事件; 无需先获取SelectionKey再关注事件
        // ServerSocketChannel将accept事件注册到Selector上
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        // 监听端口
        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            // 在有事件发生时才会向下运行
            selector.select();
            // 拿到所有发生的事件的SelectionKey
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 移除key
                iter.remove();
                if (key.isAcceptable()) {
                    log.debug("发生accept事件");
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    // 注册SocketChannel上的读事件
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);

                    // 1. 向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 50000000; i++) {
                        sb.append("a");
                    }
                    // 存储待发送的数据
                    ByteBuffer buffer = StandardCharsets.UTF_8.encode(sb.toString());

                    // 2. 返回值代表实际写入的字节数, write方法不能保证一次把内容都写到客户端
                    int write = sc.write(buffer);
                    System.out.println("write = " + write);

                    // 3. 先尝试写一次
                    // 网络发送缓冲区是有限的，不能保证一次把内容都写到客户端
                    // 如果没有写完则关注"可写事件"，剩余的内容等发送缓冲区准备好"可写"后会再次触发"可写事件"
                    // 这样做的好处是若发送大量的数据不会一直等待发送缓冲区而处理不了其它的事件(其它SocketChannel的write事件、accept事件、read事件）
                    if (buffer.hasRemaining()) { // 判断是否有剩余内容
                        // 4. 追加关注可写事件         1        +            4
                        // 只有在尝试一次写写不完的情况下才需要关注可写事件
                        scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                        // 与上行效果相同
//                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);
                        // 5. 把未写完的数据挂到SectionKey上
                        scKey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    log.debug("发生write事件");
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    System.out.println("write = " + write);
                    // 6. 清理操作，为了GC
                    if (!buffer.hasRemaining()) {
                        key.attach(null); // 需要清除buffer
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE); // 不需再关注可写事件
                    }
                }
            }
        }
    }

}
