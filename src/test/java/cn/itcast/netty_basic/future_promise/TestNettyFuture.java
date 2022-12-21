package cn.itcast.netty_basic.future_promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * <h2>演示Netty中的Future</h2>
 * <pre>
 * 1) Netty中的Future继承自JDK中的Future
 * 2) 区别于JDK中的Future, Netty中的Future不仅可以以"同步"的方式获取结果，而且可以以"异步"的方式获取结果
 * 2.1) 同步的方式获取结果: Future#get
 * 注意：在同步的方式中，线程自己去主动获取结果
 * 注意：获取结果的线程是main线程，而执行任务的线程是NioEventLoop中的线程
 * 运行结果：
 * 11:12:36 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.TestNettyFuture - 执行计算
 * 11:12:36 [DEBUG] [main] c.i.n.c.TestNettyFuture - 等待结果
 * 11:12:39 [DEBUG] [main] c.i.n.c.TestNettyFuture - 结果是:70
 * 2.2) 异步的方式获取结果: Future#addListener
 * 注意：在异步的方式中，线程自己不去主动获取结果，而是由其它线程送结果
 * 注意：获取结果的线程是NioEventLoop中的线程，而执行任务的线程也是NioEventLoop中的线程
 * 运行结果：
 * 11:16:23 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.TestNettyFuture - 执行计算
 * 11:16:26 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.TestNettyFuture - 接收结果:70
 * </pre>
 */
@Slf4j
public class TestNettyFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1. 事件循环组，对比JDK中的线程池
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next(); // 每个EventLoop对象里只有一个线程

        // 2. 提交任务
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(3000);
                return 70;
            }
        });

        // 方法1. 同步方式接收结果
        // log.debug("等待结果");
        // log.debug("结果是:{}", future.get()); // 阻塞在此

        // 方法2. 异步方式接收结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("接收结果:{}", future.getNow()); // 这里使用getNow立刻获取结果，不阻塞
            }
        });
    }

}
