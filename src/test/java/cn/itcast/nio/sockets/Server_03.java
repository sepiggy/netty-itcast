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

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugRead;

/**
 * 使用“NIO+非阻塞+多路复用(Selector)+单线程"解决Server端"忙等空转"问题
 * <br>
 * 使用run模式运行服务端
 * <br>
 * 引入Selector解决忙等空转问题，使用Selector步骤：
 * <br>
 * 1) 创建Selector: 使用Selector#open方法
 * <br>
 * 2) 获取SelectionKey：使用Channel#register将某个Channel关心的事件注册到Selector上
 * <br>
 * 3) 移除SelectionKey
 * <br>
 * 4) 处理事件 (正常处理 or 取消)
 * <br>
 * 结论：
 * <br>
 * 1. 使用"NIO+非阻塞+Selector+单线程“模式可以解决Server端”忙等空转“的问题
 * <br>
 * 2. 但读取数据的时候没有做消息边界的处理，需要进一步做优化：{@link Server_04}
 */
@Slf4j
public class Server_03 {

    public static void main(String[] args) throws IOException {

        // 1. 创建selector, 管理多个channel, 需要将channel注册到selector上:
        //    selector同时管理ServerSocketChannel和SocketChannel两个Channel
        Selector selector = Selector.open();
        System.out.println("selector.getClass() = " + selector.getClass());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 开启ServerSocketChannel非阻塞模式
        ssc.configureBlocking(false);

        // 2. 建立selector和channel的联系 (注册)
        // 注意：通过SelectionKey可以知道将来哪个Channel发生哪个事件
        // 注意：一个Selector可以管理多个Channel
        // 注意：不同的事件类型可以通过不同的SelectionKey来管理
        // 注意：也就是一个Selector对应多个SelectionKey, 一个SelectionKey管理多个Channel

        // 四种事件类型：
        // accept - 会在有连接请求时触发 (ServerSocketChannel独有)
        // connect - 客户端连接建立后触发 (SocketChannel独有)
        // read - 可读事件 (ServerSocketChannel和SocketChannel都有)
        // write - 可写事件 (ServerSocketChannel和SocketChannel都有)

        // ServerSocketChannel将accept事件注册到selector上得到sscKey作为token
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT);

        log.debug("sscKey: {}", sscKey);
        ssc.bind(new InetSocketAddress(8080));

        while (true) {

            // 3. select方法
            // 注意：select方法没有事件发生时会阻塞，不会忙等；有事件发生时会响应恢复运行 (包括已处理和未处理两种情况都会响应)
            // 注意：select方法有事件发生时要进行处理；不能置之不理；否则下次循环Selector会把未处理的key再次加入到对应的SelectedKeys中陷入死循环
            // 注意：事件处理的两种情况：
            // 注意：1. 正常处理：调用对应Channel上的accept方法(accept事件)或read方法(read事件)等
            // 注意：2. 取消事件: 调用SelectionKey#cancel方法
            selector.select();

            // 4. 处理事件, Selector#selectedKeys的返回值包含了所有已触发事件的SelectionKey
            // 注意：这里后面需要对已触发事件的SelectionKey进行删除操作，因此这里只能用迭代器进行遍历，不能使用增强for进行遍历
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 注意：处理完一个key一定要从selectedKeys集合中删除，否则下次处理就会报NPE
                // 注意：此时在这次循环作用域中还有iterator的引用可以正常进行操作
                iterator.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                // 无论是正常断开还是异常断开，都会触发一次读事件
                // 所以针对这两种情况都要分别做处理，否则服务端会因为
                // 你没有处理这次读事件不停地陷入死循环
                if (key.isAcceptable()) { // 处理ServerSocketChannel的accept事件
                    log.debug("有accept事件发生");
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    log.debug("ServerSocketChannel: {}", channel);
                    SocketChannel sc = channel.accept();
                    log.debug("SocketChannel: {}", sc);
                    sc.configureBlocking(false);
                    // SocketChannel将read事件注册到selector上得到scKey作为token
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("SectionKey: {}", scKey);
                } else if (key.isReadable()) { // 注意：处理SocketChannel读事件 (包括正常读事件和客户端关闭事件(包括正常断开和异常断开))
                    log.debug("有read事件发生");
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = channel.read(buffer);
                        if (read == -1) {  // 注意：如果是正常断开，read的方法返回值是-1
                            log.debug("{}断开连接", channel);
                            key.cancel();
                        } else {
                            buffer.flip();
                            debugRead(buffer);
                        }
                    } catch (IOException e) { // 客户端断开会抛出IOException异常，这里进行捕获
                        e.printStackTrace();
                        key.cancel(); // 注意：如果是异常断开，因为客户端断开了，因此需要反注册，将其从selectedKeys集合中删除
                    }
                }
//                key.cancel(); // The key will be removed from all of the selector's key sets during the next selection operation.
            }
        }
    }

}
