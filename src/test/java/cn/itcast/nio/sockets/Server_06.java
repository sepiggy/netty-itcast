package cn.itcast.nio.sockets;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * 使用“NIO+非阻塞+多路复用(Selector)+多线程"提高Server端运行效率
 * <br>
 * 使用run模式运行服务端
 * <br>
 * 要点：
 * <br>
 * 1) 在 {@link Server_05} 的基础上，一个Selector负责所有的accept、read、write事件拆分为：
 * Boss只负责accept事件，Worker工作组负责read和write事件
 * <br>
 * 2) 拆分原则：
 * <br>
 * 一个Selector只对应一个Thread
 * <br>
 * 一个Selector可以对应一个Channel(Boss)，也可以对应多个Channel(Worker)
 * <br>
 * 3) 拆分结果：
 * <br>
 * Boss由"1个Thread+1个Selector+1个ServerSocketChannel"组成
 * <br>
 * Worker工作组由多个Worker组成，一个Worker由“1个Thread+1个Selector+多个SocketChannel”组成
 */
@Slf4j
public class Server_06 {

    public static void main(String[] args) throws IOException {

        // Boss线程处理Accept事件
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));

        // 1. 创建"固定"数量的Worker并初始化
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger(); // 计数器，用于轮询
        while (true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) { // 连接建立
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 2. SocketChannel关联Worker中的Selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    // "round robin"轮询
                    int indexCopy = index.intValue();
                    workers[indexCopy % workers.length].register(sc);
                    log.debug("分配给workers[{}]处理read事件", indexCopy % workers.length);
                    index.getAndIncrement();
                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }
        }
    }

    static class Worker implements Runnable {

        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false; // Thread和Selector还未初始化
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public Worker(String name) {
            this.name = name;
        }

        // 初始化Thread和Selector
        public void register(SocketChannel sc) throws IOException {
            if (!start) { // 这段if守卫的代码只执行一次，保证一个Worker对应一个Thread和一个Selector
                selector = Selector.open();
                thread = new Thread(this, name); // 当前worker对象就是执行单元
                thread.start();
                start = true;
            }
            // 向队列添加了任务，但这个任务没有被执行
            queue.add(() -> {
                try {
                    // SocketChannel的读事件注册到Worker的Selector上
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    throw new RuntimeException(e);
                }
            });
            selector.wakeup(); // 使Selector#select方法不再阻塞，继续运行
        }

        /**
         * 这里简化处理: 消息边界和断开连接引发read事件以及write事件暂时忽略，本Demo重点在多线程模型
         */
        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(); // 在此阻塞 (当有read事件或调用Selector#wakup方法主动唤醒时停止阻塞)
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run(); // 真正执行 "sc.register(selector, SelectionKey.OP_READ, null);"
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) { // 不必关心accept事件
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("read...{}", channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
