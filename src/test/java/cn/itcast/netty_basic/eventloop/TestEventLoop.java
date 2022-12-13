package cn.itcast.netty_basic.eventloop;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * <h2>EventLoop接口(事件循环)和EventLoopGroup接口(事件循环组)的使用</h2>
 * <pre>
 * 对于EventLoop接口一般不会直接使用，而是使用EventLoopGroup接口，EventLoopGroup接口的实现类常见的有:
 * 1) NioEventLoopGroup: which is used for NIO Selector based Channels. NioEventLoopGroup功能最为全面，既能处理IO事件，也能向其提交普通任务，还能向其提交定时任务.
 * 2) DefaultEventLoopGroup: which must be used for the local transport. DefaultEventLoopGroup不能处理IO事件，只能向其提交普通任务和定时任务
 * 重要方法：
 * 1) <b>EventLoopGroup#next</b>: 轮询获取下一个事件循环对象
 * 2) <b>EventLoopGroup#next#execute</b>: 执行普通任务，因为EventLoopGroup接口继承自 {@link java.util.concurrent.ScheduledExecutorService}
 * 3) <b>EventLoopGroup#next#scheduleAtFixedRate</b>: 执行定时任务，因为EventLoopGroup接口继承自 {@link java.util.concurrent.ScheduledExecutorService}
 * </pre>
 */
@Slf4j
public class TestEventLoop {

    public static void main(String[] args) {

        // 1. 创建事件循环组
//        EventLoopGroup group = new NioEventLoopGroup(); // IO事件，普通任务，定时任务
//        EventLoopGroup group = new DefaultEventLoopGroup(); // 普通任务，定时任务

        /**
         * <pre>
         *     DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
         *                 "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
         * </pre>
         */
        // 若指定nThreads则按指定的参数设置线程数，否则使用默认线程数
        EventLoopGroup group = new NioEventLoopGroup(2 );

        // 2. 轮询获取下一个事件循环对象
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());

        // 3. 执行普通任务
        group.next().execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("ok");
        });

        // 4. 执行定时任务
        group.next().scheduleAtFixedRate(() -> {
            log.debug("ok");
        }, 0, 1, TimeUnit.SECONDS);

        log.debug("main");
    }

}
